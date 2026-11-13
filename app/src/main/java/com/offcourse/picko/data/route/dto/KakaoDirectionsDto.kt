package com.offcourse.picko.data.route.dto

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.route.model.RouteModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

private const val RESULT_CODE_SUCCESS = 0
private const val SECONDS_PER_MINUTE = 60

// 카카오모빌리티 자동차 길찾기(GET /v1/directions) 응답 중 쓰는 부분이다.
@Serializable
data class KakaoDirectionsDto(
    val routes: List<Route> = emptyList(),
) {
    @Serializable
    data class Route(
        val result_code: Int = -1,
        val summary: Summary? = null,
        val sections: List<Section> = emptyList(),
    )

    @Serializable
    data class Summary(
        val distance: Int = 0,
        val duration: Int = 0,
    )

    @Serializable
    data class Section(
        val roads: List<Road> = emptyList(),
    )

    /** [vertexes]는 경도, 위도 순서로 번갈아 놓인 1차원 배열이다. */
    @Serializable
    data class Road(
        val vertexes: List<Double> = emptyList(),
    )
}

/** 첫 번째 경로를 [RouteModel]로 바꾼다. 실패한 응답이거나 선을 그릴 좌표가 2개 미만이면 null. */
fun KakaoDirectionsDto.toRouteModel(): RouteModel? {
    val route = routes.firstOrNull()?.takeIf { it.result_code == RESULT_CODE_SUCCESS } ?: return null
    val points = route.sections
        .flatMap { it.roads }
        .flatMap { it.vertexes.chunked(2) }
        .filter { it.size == 2 }
        .map { (longitude, latitude) -> Coordinate(latitude = latitude, longitude = longitude) }
        .fold(mutableListOf<Coordinate>()) { acc, point ->
            // 도로가 이어지는 곳의 중복 꼭짓점은 한 번만 둔다.
            if (acc.lastOrNull() != point) acc.add(point)
            acc
        }
    if (points.size < 2) return null

    val seconds = route.summary?.duration ?: 0
    return RouteModel(
        points = points.toImmutableList(),
        durationMinutes = (seconds + SECONDS_PER_MINUTE / 2) / SECONDS_PER_MINUTE,
    )
}
