package com.offcourse.picko.data.place.dto

import kotlinx.serialization.Serializable

// 카카오 로컬 키워드 검색(GET /v2/local/search/keyword.json) 응답 중 쓰는 부분이다.
// 좌표 x(경도), y(위도)와 distance(m)는 문자열로 온다.
@Serializable
data class KakaoKeywordSearchDto(
    val documents: List<Document> = emptyList(),
) {
    @Serializable
    data class Document(
        val id: String = "",
        val place_name: String = "",
        val x: String = "",
        val y: String = "",
    )
}
