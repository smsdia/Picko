package com.offcourse.picko.data.place.datasource

import com.offcourse.picko.data.place.dto.PickupRecommendDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Picko 서버의 픽업 후보 추천(스웨거 `/v3/api-docs`). 차량 위치와 목적지만 받고 탑승자 위치는 받지 않는다. */
interface PickupRecommendApi {
    @GET("api/pickup/recommend")
    suspend fun recommend(
        @Query("vLat") vehicleLat: Double,
        @Query("vLng") vehicleLng: Double,
        @Query("dLat") destinationLat: Double,
        @Query("dLng") destinationLng: Double,
        @Query("limit") limit: Int,
    ): PickupRecommendDto
}
