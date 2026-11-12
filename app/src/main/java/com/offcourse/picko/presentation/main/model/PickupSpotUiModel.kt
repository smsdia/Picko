package com.offcourse.picko.presentation.main.model

import androidx.compose.runtime.Immutable

@Immutable
data class PickupSpotUiModel(
    val latitude: Double,
    val longitude: Double,
)
