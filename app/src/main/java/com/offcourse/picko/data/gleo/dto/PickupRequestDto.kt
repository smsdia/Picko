package com.offcourse.picko.data.gleo.dto

import com.offcourse.picko.data.gleo.model.PickupRequestModel
import kotlinx.serialization.Serializable

// schema.json 의 REQUEST_PICKUP 파라미터와 같은 모양이다.
@Serializable
data class PickupRequestDto(
    val passenger: String,
    val place: String? = null,
)

/** 탑승자 이름이 비어 있으면 null. 공백은 다듬는다. */
fun PickupRequestDto.toModel(): PickupRequestModel? {
    val name = passenger.trim()
    if (name.isEmpty()) return null

    return PickupRequestModel(
        passenger = name,
        place = place?.trim()?.takeIf { it.isNotEmpty() },
    )
}
