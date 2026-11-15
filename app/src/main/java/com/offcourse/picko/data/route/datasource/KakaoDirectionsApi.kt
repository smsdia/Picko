package com.offcourse.picko.data.route.datasource

import com.offcourse.picko.data.route.dto.KakaoDirectionsDto
import retrofit2.http.GET
import retrofit2.http.Query

/** 카카오모빌리티 자동차 길찾기. 좌표는 `경도,위도` 문자열이다. */
interface KakaoDirectionsApi {
    @GET("v1/directions")
    suspend fun directions(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
    ): KakaoDirectionsDto
}
