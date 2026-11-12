package com.offcourse.picko.core.designsystem.component.indicator

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.core.common.extensions.dropShadowLevel1
import kotlinx.coroutines.delay

private const val DotCount = 3

// Figma에 점이 바뀌는 속도가 없어서 임의로 정했다. TODO(figma): 속도가 정해지면 맞춘다.
private const val DotIntervalMillis = 400L
private val ActiveDotSize = 24.dp
private val InactiveDotSize = 16.dp

/** 점 세 개 중 하나가 순서대로 커지며 로딩 중임을 알린다. Figma의 `state=1~3`을 이어 붙인 동작이다. */
@Composable
fun PickoLoadingIndicator(modifier: Modifier = Modifier) {
    var activeIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(DotIntervalMillis)
            activeIndex = (activeIndex + 1) % DotCount
        }
    }

    Row(
        modifier = modifier.size(width = 120.dp, height = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DotCount) { index ->
            val isActive = index == activeIndex
            val dotSize by animateDpAsState(
                targetValue = if (isActive) ActiveDotSize else InactiveDotSize,
                animationSpec = tween(durationMillis = 200),
                label = "dotSize",
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .dropShadowLevel1(CircleShape)
                    .background(
                        color = if (isActive) PickoTheme.colors.accentText else PickoTheme.colors.dotInactive,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@PickoCarPreview
@Composable
private fun PickoLoadingIndicatorPreview() {
    PickoPreviewSurface {
        PickoLoadingIndicator()
    }
}
