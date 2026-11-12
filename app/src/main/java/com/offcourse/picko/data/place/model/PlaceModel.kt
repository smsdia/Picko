package com.offcourse.picko.data.place.model

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.model.Coordinate

/** 주변 장소를 추천한 이유. 화면은 이 값에 맞는 문구를 `strings.xml`에서 고른다. */
sealed interface PickupReason {
    /** 탑승자가 걸어서 [minutes]분이면 닿는다. */
    data class ShortWalk(val minutes: Int) : PickupReason

    data object NearEntrance : PickupReason

    data object EasyStop : PickupReason
}

@Immutable
data class PlaceModel(
    val id: String,
    val name: String,
    val coordinate: Coordinate,
    val distanceKm: Int,
    val durationMinutes: Int,
    // null 이면 통행료가 없다.
    val tollFeeWon: Int? = null,
    // 주변 장소 추천에서만 채운다.
    val reason: PickupReason? = null,
)
