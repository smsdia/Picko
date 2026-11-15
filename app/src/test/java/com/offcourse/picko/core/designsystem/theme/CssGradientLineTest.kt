package com.offcourse.picko.core.designsystem.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

class CssGradientLineTest {
    private val tolerance = 0.01f

    @Test
    fun 구십도는_왼쪽에서_오른쪽으로_가로질러_그린다() {
        val (from, to) = cssGradientLine(90f, Size(200f, 100f))

        assertOffset(Offset(0f, 50f), from)
        assertOffset(Offset(200f, 50f), to)
    }

    @Test
    fun 백팔십도는_위에서_아래로_세로질러_그린다() {
        val (from, to) = cssGradientLine(180f, Size(200f, 100f))

        assertOffset(Offset(100f, 0f), from)
        assertOffset(Offset(100f, 100f), to)
    }

    @Test
    fun 정사각형의_사십오도는_모서리에서_모서리까지_그린다() {
        val (from, to) = cssGradientLine(135f, Size(100f, 100f))

        assertOffset(Offset(0f, 0f), from)
        assertOffset(Offset(100f, 100f), to)
    }

    private fun assertOffset(expected: Offset, actual: Offset) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
    }
}
