package com.offcourse.picko.data.navi.model

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.model.Coordinate

/** 차량 내비에 넘길 목적지 */
@Immutable
data class NaviDestinationModel(
    val name: String,
    val coordinate: Coordinate,
)

/** 내비에 안내를 요청한 결과. 내비의 경로 탐색 오류는 요청 뒤 이벤트로 늦게 오므로 [Requested]도 탐색 성공은 아니다. */
enum class NaviGuidanceResult {
    /** 요청을 보냈고 잠시 기다리는 동안 오류가 오지 않았다. */
    Requested,

    /** 내비에 연결하지 못했다. */
    NotConnected,

    /** 배터리가 부족하다. */
    LowBattery,

    /** 경로를 탐색할 수 있는 거리를 넘었다. */
    RouteTooFar,

    /** 현재 위치와 목적지가 너무 가깝다. */
    RouteTooClose,

    /** 경로 탐색 서버 오류 등 그 밖의 탐색 실패 */
    RouteFailed,
}

/** 안내를 시작하지 않고 미리 계산한 차량의 소요 시간과 거리 */
@Immutable
data class NaviTripModel(
    val durationMinutes: Int,
    val distanceMeters: Int,
)
