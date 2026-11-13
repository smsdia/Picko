package com.offcourse.picko.data.route.model

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.model.Coordinate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** 지도에 선으로 그릴 경로. [points]는 출발지부터 도착지까지 순서대로 이어진다. */
@Immutable
data class RouteModel(
    val points: ImmutableList<Coordinate>,
    val durationMinutes: Int,
)

/**
 * 이동한 [position]에서 시작하는 경로. 가장 가까운 꼭짓점까지는 지나간 것으로 보고 앞부분을 지운다.
 * 길을 벗어났는지는 따지지 않는 근사다.
 */
fun RouteModel.advanceTo(position: Coordinate): RouteModel =
    copy(points = remainingPointsFrom(points, position).toImmutableList())

/** [points] 중 [position] 이후에 남은 구간. 첫 점은 [position]이다. 지도 카메라를 남은 구간에 맞출 때 쓴다. */
fun remainingPointsFrom(points: List<Coordinate>, position: Coordinate): List<Coordinate> {
    val nearest = points.indices.minByOrNull { index -> points[index].squaredDistanceTo(position) } ?: return points
    val rest = points.drop(nearest + 1).ifEmpty { listOf(points.last()) }

    return listOf(position) + rest
}

// 가까운 점을 가릴 때만 쓰는 값이라 단위 변환 없이 위도·경도 차이의 제곱합으로 비교한다.
private fun Coordinate.squaredDistanceTo(other: Coordinate): Double {
    val dLat = latitude - other.latitude
    val dLon = longitude - other.longitude
    return dLat * dLat + dLon * dLon
}

/** 픽업 지점으로 가는 차량 경로와 탑승자의 도보 경로. */
@Immutable
data class PickupRoutes(
    val vehicle: RouteModel,
    val passengerWalk: RouteModel,
)

/** 픽업을 마친 뒤 목적지까지의 경로. */
@Immutable
data class DestinationRoute(
    val destinationName: String,
    val route: RouteModel,
    val distanceKm: Int,
)
