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
        // `poi_CS2`처럼 POI에서 왔거나 `route_sample`처럼 경로 위의 점이다.
        val source: String = "",
        val label: String = "",
        val category: String = "",
        val walkFromDestinationMin: Double = 0.0,
        val vehicleDistanceM: Double = 0.0,
        val vehicleEtaMin: Double = 0.0,
        // 금지 구역(소화장치, 횡단보도 등)에 겹치면 true. 서버가 이런 지점을 뒤로 보낸다.
        val inExcludedZone: Boolean = false,
    )
}
