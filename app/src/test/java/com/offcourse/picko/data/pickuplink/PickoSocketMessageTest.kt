package com.offcourse.picko.data.pickuplink

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.pickuplink.datasource.backoffMillis
import com.offcourse.picko.data.pickuplink.dto.PickoSocketMessage
import com.offcourse.picko.data.pickuplink.dto.parsePickoSocketMessage
import com.offcourse.picko.data.pickuplink.dto.vehicleLocationMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PickoSocketMessageTest {
    @Test
    fun `탑승자 위치 메시지를 읽는다`() {
        val message = parsePickoSocketMessage("""{"type":"pedestrian_location","lat":37.5,"lng":127.0}""")

        assertEquals(PickoSocketMessage.PedestrianLocation(Coordinate(37.5, 127.0)), message)
    }

    @Test
    fun `세션 취소 메시지를 읽는다`() {
        val message = parsePickoSocketMessage("""{"type":"session_canceled","reason":"driver_canceled"}""")

        assertEquals(PickoSocketMessage.SessionCanceled("driver_canceled"), message)
    }

    @Test
    fun `알 수 없는 type과 깨진 형식은 무시한다`() {
        assertNull(parsePickoSocketMessage("""{"type":"unknown","lat":1.0}"""))
        assertNull(parsePickoSocketMessage("""{"type":"pedestrian_location","lat":1.0}"""))
        assertNull(parsePickoSocketMessage("not json"))
    }

    @Test
    fun `서버 프로토콜대로 차량 위치를 보낸다`() {
        val json = vehicleLocationMessage(Coordinate(37.5, 127.0))

        assertEquals("""{"type":"vehicle_location","lat":37.5,"lng":127.0}""", json)
    }

    @Test
    fun `재접속 대기는 1초에서 두 배씩 늘고 30초를 넘지 않는다`() {
        assertEquals(
            listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 30_000L, 30_000L),
            (0..6).map(::backoffMillis),
        )
    }
}
