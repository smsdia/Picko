package com.offcourse.picko.data.gleo.model

import androidx.compose.runtime.Immutable

@Immutable
data class PickupRequestModel(
    val passenger: String,
    // null 이면 현재 위치 기준
    val place: String? = null,
)
