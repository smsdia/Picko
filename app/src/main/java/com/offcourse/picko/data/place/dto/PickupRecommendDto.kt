package com.offcourse.picko.data.place.dto

import kotlinx.serialization.Serializable

// Picko 서버 `GET /api/pickup/recommend` 응답 중 쓰는 부분이다(2026-11-15 실제 호출로 확인, 스웨거에는 응답 스키마가 없다).
@Serializable
data class PickupRecommendDto(
    val recommendations: List<Item> = emptyList(),
) {
    @Serializable
    data class Item(
        val lat: Double,
        val lng: Double,
        val label: String = "",
        val vehicleDistanceM: Double = 0.0,
        val vehicleEtaMin: Double = 0.0,
    )
}
