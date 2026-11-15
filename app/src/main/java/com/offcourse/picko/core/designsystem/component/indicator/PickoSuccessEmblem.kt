package com.offcourse.picko.core.designsystem.component.indicator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.theme.PickoTheme

// Figma `ic_complete`의 기준 크기와 구성(172 안에 바깥 고리 반지름 86, 안쪽 반지름 66, 체크 선 굵기 8)
private const val BaseSize = 172f
private const val CheckStrokeWidth = 8f

/** 완료를 알리는 원형 체크 표시. 비율은 Figma `ic_complete`와 같고 [size]에 맞춰 커진다. */
@Composable
fun PickoSuccessEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 172.dp,
) {
    val colors = PickoTheme.colors

    Canvas(modifier = modifier.size(size)) {
        val scale = this.size.width / BaseSize
        val center = Offset(this.size.width / 2, this.size.height / 2)

        drawCircle(color = colors.emblemRing, radius = 86f * scale, center = center)
        drawCircle(color = colors.emblemFill, radius = 66f * scale, center = center)

        val check = Path().apply {
            moveTo(64f * scale, 87f * scale)
            lineTo(79f * scale, 101f * scale)
            lineTo(108f * scale, 70f * scale)
        }
        drawPath(
            path = check,
            color = colors.accent,
            style = Stroke(
                width = CheckStrokeWidth * scale,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

@PickoCarPreview
@Composable
private fun PickoSuccessEmblemPreview() {
    PickoPreviewSurface {
        PickoSuccessEmblem()
    }
}
