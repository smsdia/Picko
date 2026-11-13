package com.offcourse.picko.data.route

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.route.dto.KakaoDirectionsDto
import com.offcourse.picko.data.route.dto.toRouteModel
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KakaoDirectionsDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun parse(text: String) = json.decodeFromString<KakaoDirectionsDto>(text)

    @Test
    fun `성공 응답의 꼭짓점을 경도 위도 순서로 읽고 중복 꼭짓점을 합친다`() {
        val route = parse(
            """
            {"trans_id":"x","routes":[{"result_code":0,"result_msg":"길찾기 성공",
            "summary":{"distance":1200,"duration":150},
            "sections":[{"roads":[
              {"vertexes":[127.1,37.4,127.2,37.5]},
              {"vertexes":[127.2,37.5,127.3,37.6]}
            ]}]}]}
            """.trimIndent(),
        ).toRouteModel()

        assertEquals(
            listOf(
                Coordinate(37.4, 127.1),
                Coordinate(37.5, 127.2),
                Coordinate(37.6, 127.3),
            ),
            route?.points,
        )
        assertEquals(3, route?.durationMinutes)
    }

    @Test
    fun `실패 코드 응답은 null`() {
        val route = parse("""{"routes":[{"result_code":104,"result_msg":"출발지와 도착지가 너무 가깝습니다"}]}""")

        assertNull(route.toRouteModel())
    }

    @Test
    fun `좌표가 2개 미만이면 null`() {
        val route = parse(
            """{"routes":[{"result_code":0,"sections":[{"roads":[{"vertexes":[127.1,37.4]}]}]}]}""",
        )

        assertNull(route.toRouteModel())
    }
}
