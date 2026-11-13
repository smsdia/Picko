package com.offcourse.picko.data.vehicle

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.common.model.distanceMetersTo
import com.offcourse.picko.data.vehicle.repositoryimpl.buildDemoPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoPathTest {
    private val start = Coordinate(37.0, 127.0)
    private val corner = Coordinate(37.01, 127.0)
    private val end = Coordinate(37.01, 127.01)

    @Test
    fun `출발점으로 시작하고 끝 지점으로 끝난다`() {
        val path = buildDemoPath(listOf(start, corner, end), stepMeters = 40.0)

        assertEquals(start, path.first())
        assertEquals(end, path.last())
    }

    @Test
    fun `점이 촘촘한 경로에서도 걸음 간격으로 다시 찍는다`() {
        val dense = (0..1000).map { Coordinate(37.0 + it * 0.00001, 127.0) } // 약 1.1m 간격

        val path = buildDemoPath(dense, stepMeters = 40.0)

        assertEquals(dense.last(), path.last())
        assertTrue(path.size in 25..30)
    }

    @Test
    fun `이웃한 점 사이의 거리는 걸음 간격을 넘지 않는다`() {
        val path = buildDemoPath(listOf(start, corner, end), stepMeters = 40.0)

        assertTrue(path.zipWithNext().all { (a, b) -> a.distanceMetersTo(b) <= 40.5 })
    }
}
