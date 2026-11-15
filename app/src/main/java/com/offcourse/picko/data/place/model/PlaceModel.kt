package com.offcourse.picko.data.place.model

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.model.Coordinate

@Immutable
data class PlaceModel(
    val id: String,
    val name: String,
    val coordinate: Coordinate,
    val distanceKm: Int,
    val durationMinutes: Int,
)
