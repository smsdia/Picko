package com.offcourse.picko.data.place

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.place.dto.PickupRecommendDto
import com.offcourse.picko.data.place.repositoryimpl.toPlace
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PickupRecommendMappingTest {
    private val json = Json { ignoreUnknownKeys = true }

    // 2026-11-15 `GET /api/pickup/recommend`를 실제로 불러 받은 응답에서 줄인 것
    private val response = """
        {"count":2,"recommendations":[
          {"lat":37.498939,"lng":127.029887,"source":"poi_CS2","label":"GS25 강남타운점","category":"편의점",
           "walkFromDestinationM":223.3,"walkFromDestinationMin":2.97,"vehicleDistanceM":2479.7,"vehicleEtaMin":7.96,
           "detourM":20.7,"poiBonus":0.8,"walkFromOsrm":true,"score":2.19,"inExcludedZone":false,"excludedReasons":[],
           "reasons":["walk=223m (OSRM)"]},
          {"lat":37.498752,"lng":127.029887,"source":"route_sample","label":"경로상 지점","category":"경로",
           "walkFromDestinationM":204.6,"walkFromDestinationMin":2.73,"vehicleDistanceM":2459.0,"vehicleEtaMin":7.89,
           "inExcludedZone":false,"excludedReasons":[]}
        ]}
    """.trimIndent()

    @Test
    fun `서버 응답에서 쓰는 필드만 읽는다`() {
        val dto = json.decodeFromString<PickupRecommendDto>(response)

        assertEquals(2, dto.recommendations.size)
        assertEquals("GS25 강남타운점", dto.recommendations[0].label)
    }

    @Test
    fun `POI 후보는 이름과 좌표와 반올림한 거리와 올림한 소요 시간으로 바꾼다`() {
        val place = json.decodeFromString<PickupRecommendDto>(response).recommendations[0].toPlace(0)

        assertEquals("GS25 강남타운점", place.name)
        assertEquals(Coordinate(37.498939, 127.029887), place.coordinate)
        assertEquals(2, place.distanceKm)
        assertEquals(8, place.durationMinutes)
    }

    @Test
    fun `후보마다 다른 ID를 준다`() {
        val items = json.decodeFromString<PickupRecommendDto>(response).recommendations
        val ids = items.mapIndexed { index, item -> item.toPlace(index).id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `후보가 없는 응답은 빈 목록이다`() {
        val dto = json.decodeFromString<PickupRecommendDto>("""{"count":0,"recommendations":[]}""")

        assertTrue(dto.recommendations.isEmpty())
    }
}
