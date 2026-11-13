package com.offcourse.picko.presentation.pickup.component

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize

/** 배지를 기준으로 라벨이 놓이는 쪽. 선언 순서가 기본 우선순위다. */
internal enum class LabelSide { End, Start, Top, Bottom }

internal class LabelPlacement(val side: LabelSide, val rect: IntRect)

/**
 * 라벨을 놓을 자리를 고른다. [preferred]가 아직 들어맞으면 그대로 두고(위치가 조금씩 바뀔 때 라벨이 튀지 않게),
 * 아니면 오른쪽, 왼쪽, 위, 아래 순서로 [bounds] 안에 들어오고 [occupied]와 겹치지 않는 첫 자리를 쓴다.
 * 어디도 맞지 않으면 오른쪽 자리를 [bounds] 안으로 밀어 넣는다.
 */
internal fun placeLabel(
    badge: IntRect,
    labelSize: IntSize,
    bounds: IntRect,
    occupied: List<IntRect>,
    gap: Int,
    preferred: LabelSide? = null,
): LabelPlacement {
    fun candidate(side: LabelSide): IntRect {
        val topLeft = when (side) {
            LabelSide.End -> IntOffset(badge.right + gap, badge.center.y - labelSize.height / 2)
            LabelSide.Start -> IntOffset(badge.left - gap - labelSize.width, badge.center.y - labelSize.height / 2)
            LabelSide.Top -> IntOffset(badge.center.x - labelSize.width / 2, badge.top - gap - labelSize.height)
            LabelSide.Bottom -> IntOffset(badge.center.x - labelSize.width / 2, badge.bottom + gap)
        }

        return IntRect(topLeft, labelSize)
    }

    fun fits(rect: IntRect): Boolean =
        rect.left >= bounds.left && rect.right <= bounds.right &&
            rect.top >= bounds.top && rect.bottom <= bounds.bottom &&
            occupied.none { it.overlaps(rect) }

    val order = listOfNotNull(preferred) + LabelSide.entries.filter { it != preferred }
    order.forEach { side ->
        val rect = candidate(side)
        if (fits(rect)) return LabelPlacement(side, rect)
    }

    val fallback = candidate(LabelSide.End)
    val x = fallback.left.coerceIn(bounds.left, maxOf(bounds.left, bounds.right - labelSize.width))
    val y = fallback.top.coerceIn(bounds.top, maxOf(bounds.top, bounds.bottom - labelSize.height))

    return LabelPlacement(LabelSide.End, IntRect(IntOffset(x, y), labelSize))
}
