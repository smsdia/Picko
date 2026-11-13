package com.offcourse.picko.data.route

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.common.model.distanceMetersTo
import com.offcourse.picko.data.route.model.RouteModel
import com.offcourse.picko.data.route.model.advanceTo
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteModelTest {
    private val a = Coordinate(37.0, 127.0)
    private val b = Coordinate(37.1, 127.0)
    private val c = Coordinate(37.2, 127.0)
    private val d = Coordinate(37.3, 127.0)
    private val route = RouteModel(points = persistentListOf(a, b, c, d), durationMinutes = 10)

    @Test
    fun `지나간 꼭짓점은 지우고 현재 위치에서 시작한다`() {
        val position = Coordinate(37.11, 127.0)

        assertEquals(listOf(position, c, d), route.advanceTo(position).points)
    }

    @Test
    fun `도착지 가까이에서는 현재 위치와 도착지만 남는다`() {
        val position = Coordinate(37.31, 127.0)

        assertEquals(listOf(position, d), route.advanceTo(position).points)
    }

    @Test
    fun `소요 시간은 그대로 둔다`() {
        assertEquals(10, route.advanceTo(Coordinate(37.15, 127.0)).durationMinutes)
    }

    @Test
    fun `위도 0_001도는 약 111m`() {
        val meters = a.distanceMetersTo(Coordinate(37.001, 127.0))

        assertTrue(meters in 110.0..112.0)
    }
}
