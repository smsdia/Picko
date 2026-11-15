package com.offcourse.picko.data.place.model

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.model.Coordinate

/** 픽업 후보를 추천한 이유. 화면은 이 값에 맞는 문구를 `strings.xml`에서 고른다. */
sealed interface PickupReason {
    /** 목적지에서 걸어서 [minutes]분이면 닿는다. */
    data class ShortWalk(val minutes: Int) : PickupReason

    /** 목적지 근처의 [category](편의점, 카페 등) 장소라 찾기 쉽다. 목적지에서 걸어서 [minutes]분이다. */
    data class NearLandmark(val category: String, val minutes: Int) : PickupReason

    /** 차량이 가는 길 위에 있어 돌아가지 않아도 된다. 목적지에서 걸어서 [minutes]분이다. */
    data class OnRoute(val minutes: Int) : PickupReason
}

@Immutable
data class PlaceModel(
    val id: String,
    val name: String,
    val coordinate: Coordinate,
    val distanceKm: Int,
    val durationMinutes: Int,
    // 서버 추천에서만 채운다.
    val reason: PickupReason? = null,
)
