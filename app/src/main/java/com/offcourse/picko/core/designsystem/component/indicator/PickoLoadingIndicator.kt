package com.offcourse.picko.core.designsystem.component.indicator

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.offcourse.picko.core.common.extensions.dropShadowLevel1
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import kotlin.math.abs
import kotlin.math.min

private const val DotCount = 3

// Figma에 점이 바뀌는 속도가 없어서 임의로 정했다. TODO(figma): 속도가 정해지면 맞춘다.
private const val DotIntervalMillis = 400
private val ActiveDotSize = 24.dp
private val InactiveDotSize = 16.dp

/**
 * 점 세 개가 물결처럼 차례로 커지며 로딩 중임을 알린다. Figma의 `state=1~3`을 이어 붙인 동작이다.
 * 하나의 연속된 값(phase)으로 세 점의 크기와 색을 함께 정해서 점 사이를 끊김 없이 넘어간다.
 */
@Composable
fun PickoLoadingIndicator(modifier: Modifier = Modifier) {
    val activeColor = PickoTheme.colors.accentText
    val inactiveColor = PickoTheme.colors.dotInactive
    val inactiveScale = InactiveDotSize / ActiveDotSize

    val transition = rememberInfiniteTransition(label = "loadingDots")
    // 0부터 DotCount까지 일정한 속도로 돈다. 값이 i에 가까울수록 i번째 점이 강조된다.
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = DotCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = DotCount * DotIntervalMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "loadingDotsPhase",
    )

    Row(
        modifier = modifier.size(width = 120.dp, height = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DotCount) { index ->
            // 점마다 가장 큰 크기의 칸을 고정해 두고 그 안에서 커져서, 옆 점의 위치가 밀리지 않는다.
            Box(
                modifier = Modifier.size(ActiveDotSize),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(ActiveDotSize)
                        .graphicsLayer {
                            val scale = lerp(inactiveScale, 1f, emphasis(phase, index))
                            scaleX = scale
                            scaleY = scale
                        }
                        .dropShadowLevel1(CircleShape)
                        .drawBehind {
                            drawCircle(lerp(inactiveColor, activeColor, emphasis(phase, index)))
                        },
                )
            }
        }
    }
}

/** [phase]가 [index]에 가까울수록 1에 가깝고, 한 칸 이상 떨어지면 0이다. 처음과 끝은 이어진다. */
private fun emphasis(phase: Float, index: Int): Float {
    val distance = abs(phase - index)
    val circular = min(distance, DotCount - distance)
    val t = (1f - circular).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

@PickoCarPreview
@Composable
private fun PickoLoadingIndicatorPreview() {
    PickoPreviewSurface {
        PickoLoadingIndicator()
    }
}
