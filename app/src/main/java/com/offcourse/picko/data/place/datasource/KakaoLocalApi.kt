package com.offcourse.picko.data.place.datasource

import com.offcourse.picko.data.place.dto.KakaoKeywordSearchDto
import retrofit2.http.GET
import retrofit2.http.Query

/** 카카오 로컬 키워드 검색. [x], [y]는 기준 좌표(경도, 위도)로, 정확도순을 유지하면서 그 근처를 우선한다. */
interface KakaoLocalApi {
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Query("query") query: String,
        @Query("x") x: Double,
        @Query("y") y: Double,
        @Query("size") size: Int,
    ): KakaoKeywordSearchDto
}
