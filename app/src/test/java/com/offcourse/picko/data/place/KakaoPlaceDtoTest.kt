package com.offcourse.picko.data.place

import com.offcourse.picko.data.place.dto.KakaoKeywordSearchDto
import com.offcourse.picko.data.route.dto.KakaoDirectionsDto
import com.offcourse.picko.data.route.dto.toTripSummary
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KakaoPlaceDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `키워드 검색 응답에서 이름과 문자열 좌표를 읽는다`() {
        val dto = json.decodeFromString<KakaoKeywordSearchDto>(
            """
            {"meta":{"total_count":1},"documents":[
              {"id":"1","place_name":"선릉역 2호선","x":"127.049","y":"37.504","distance":"120","address_name":"서울"}
            ]}
            """.trimIndent(),
        )

        val document = dto.documents.single()
        assertEquals("선릉역 2호선", document.place_name)
        assertEquals(127.049, document.x.toDouble(), 0.0)
        assertEquals(37.504, document.y.toDouble(), 0.0)
    }

    @Test
    fun `길찾기 요약에서 거리 시간 통행료를 읽는다`() {
        val summary = json.decodeFromString<KakaoDirectionsDto>(
            """{"routes":[{"result_code":0,"summary":{"distance":12393,"duration":1440,"fare":{"taxi":20000,"toll":3000}}}]}""",
        ).toTripSummary()

        assertEquals(12_393, summary?.distanceMeters)
        assertEquals(1_440, summary?.durationSeconds)
        assertEquals(3_000, summary?.tollWon)
    }

    @Test
    fun `통행료 정보가 없으면 0`() {
        val summary = json.decodeFromString<KakaoDirectionsDto>(
            """{"routes":[{"result_code":0,"summary":{"distance":500,"duration":60}}]}""",
        ).toTripSummary()

        assertEquals(0, summary?.tollWon)
    }

    @Test
    fun `실패한 길찾기는 요약이 없다`() {
        val summary = json.decodeFromString<KakaoDirectionsDto>(
            """{"routes":[{"result_code":104,"result_msg":"너무 가깝다"}]}""",
        ).toTripSummary()

        assertNull(summary)
    }
}
