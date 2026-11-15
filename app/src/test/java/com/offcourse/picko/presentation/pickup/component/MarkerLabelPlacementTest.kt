package com.offcourse.picko.presentation.pickup.component

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerLabelPlacementTest {
    private val bounds = IntRect(0, 0, 1000, 600)
    private val labelSize = IntSize(300, 100)
    private val gap = 10

    private fun badgeAt(x: Int, y: Int) = IntRect(x - 40, y - 40, x + 40, y + 40)

    @Test
    fun `자리가 있으면 오른쪽에 놓는다`() {
        val badge = badgeAt(200, 300)

        val placement = placeLabel(badge, labelSize, bounds, listOf(badge), gap)

        assertEquals(LabelSide.End, placement.side)
        assertEquals(badge.right + gap, placement.rect.left)
    }

    @Test
    fun `오른쪽이 화면을 벗어나면 왼쪽에 놓는다`() {
        val badge = badgeAt(900, 300)

        val placement = placeLabel(badge, labelSize, bounds, listOf(badge), gap)

        assertEquals(LabelSide.Start, placement.side)
    }

    @Test
    fun `양옆이 모두 좁으면 위에 놓는다`() {
        val narrow = IntRect(0, 0, 400, 600)
        val badge = badgeAt(200, 300)

        val placement = placeLabel(badge, labelSize, narrow, listOf(badge), gap)

        assertEquals(LabelSide.Top, placement.side)
        assertTrue(placement.rect.bottom <= badge.top)
    }

    @Test
    fun `위도 모자라면 아래에 놓는다`() {
        val narrow = IntRect(0, 0, 400, 600)
        val badge = badgeAt(200, 60)

        val placement = placeLabel(badge, labelSize, narrow, listOf(badge), gap)

        assertEquals(LabelSide.Bottom, placement.side)
    }

    @Test
    fun `다른 마커의 배지와 겹치면 다른 쪽을 쓴다`() {
        val badge = badgeAt(200, 300)
        val other = badgeAt(330, 300)

        val placement = placeLabel(badge, labelSize, bounds, listOf(badge, other), gap)

        assertTrue(placement.side != LabelSide.End)
        assertTrue(!placement.rect.overlaps(other))
    }

    @Test
    fun `이전에 고른 쪽이 아직 맞으면 그대로 둔다`() {
        val badge = badgeAt(500, 300)

        val placement = placeLabel(badge, labelSize, bounds, listOf(badge), gap, preferred = LabelSide.Start)

        assertEquals(LabelSide.Start, placement.side)
    }

    @Test
    fun `이전에 고른 쪽이 맞지 않으면 우선순위대로 다시 고른다`() {
        val badge = badgeAt(100, 300)

        val placement = placeLabel(badge, labelSize, bounds, listOf(badge), gap, preferred = LabelSide.Start)

        assertEquals(LabelSide.End, placement.side)
    }

    @Test
    fun `어디도 맞지 않으면 화면 안으로 밀어 넣는다`() {
        val tiny = IntRect(0, 0, 200, 100)
        val badge = badgeAt(100, 50)

        val placement = placeLabel(badge, labelSize, tiny, listOf(badge), gap)

        assertTrue(placement.rect.left >= tiny.left)
        assertTrue(placement.rect.top >= tiny.top)
    }
}
