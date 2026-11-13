package com.offcourse.picko.core.common.model

import androidx.compose.runtime.Immutable
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Immutable
data class Coordinate(
    val latitude: Double,
    val longitude: Double,
)

private const val EARTH_RADIUS_METERS = 6_371_000.0

/** 두 좌표 사이의 지표면 거리(m). 하버사인 공식이다. */
fun Coordinate.distanceMetersTo(other: Coordinate): Double {
    val lat1 = Math.toRadians(latitude)
    val lat2 = Math.toRadians(other.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(other.longitude - longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)

    return EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))
}
