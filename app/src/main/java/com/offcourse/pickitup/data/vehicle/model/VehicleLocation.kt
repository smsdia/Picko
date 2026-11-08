package com.offcourse.pickitup.data.vehicle.model

/** Pleos SDK 타입을 노출하지 않기 위한 차량 위치 모델. */
data class VehicleLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val timestampMillis: Long,
)
