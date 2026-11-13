package com.offcourse.picko.data.navi.model

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.model.Coordinate

/** 차량 내비에 넘길 목적지 */
@Immutable
data class NaviDestinationModel(
    val name: String,
    val coordinate: Coordinate,
)

/** 안내를 시작하지 않고 미리 계산한 차량의 소요 시간과 거리 */
@Immutable
data class NaviTripModel(
    val durationMinutes: Int,
    val distanceMeters: Int,
)
