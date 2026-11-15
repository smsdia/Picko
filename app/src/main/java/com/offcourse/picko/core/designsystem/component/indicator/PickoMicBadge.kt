package com.offcourse.picko.core.designsystem.component.indicator

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme

// Figma `PICKO / Aurora Halo`(라이트·다크 동일). 각 줄은 원 반지름, 선 두께, 투명도, 흐림(sigma)이다.
// 중심은 마이크 원과 같다. Core edge 선(두께 2.88)이 원의 테두리 역할을 하므로 원 지름은 그 선의 안쪽 지름(197.28dp)이고, 나머지 층은 원 바깥으로 번진다.
private class HaloLayer(val radius: Float, val strokeWidth: Float, val alpha: Float, val blurSigma: Float)

private val HaloLayers = listOf(
    HaloLayer(radius = 124.2f, strokeWidth = 39.6f, alpha = 0.48f, blurSigma = 21.6f),
    HaloLayer(radius = 117.36f, strokeWidth = 21.6f, alpha = 0.65f, blurSigma = 12.24f),
    HaloLayer(radius = 107.64f, strokeWidth = 9.36f, alpha = 0.75f, blurSigma = 3.96f),
    HaloLayer(radius = 100.08f, strokeWidth = 2.88f, alpha = 0.8f, blurSigma = 0f),
)

// Figma ANGULAR 그라데이션 정지점. 위치는 0..1이고 3시 방향에서 시계 방향으로 간다.
private val HaloStops = arrayOf(
    0.00f to Color(0xFF32D9DB),
    0.22f to Color(0xFF70F0D1),
    0.48f to Color(0xEB416DE9),
    0.72f to Color(0xFA8779FF),
    1.00f to Color(0xFF32D9DB),
)

private val MicCircleSize = 197.28.dp

/** 마이크 원 아래끝에서 그 아래 제목 글자까지의 간격(시안). halo 가 번지는 자리를 비운다. */
val PickoMicLabelGap = 100.dp

private const val HaloRotationMillis = 8000

// SVG stdDeviation(sigma)을 Android BlurMaskFilter 반지름으로 바꾸는 계수
private const val BlurRadiusPerSigma = 1.73f

/**
 * 음성을 기다리고 있음을 알리는 원형 마이크 표시. 바깥 halo가 천천히 돈다.
 * [onDim]이 true 이면 dim 위에 얹는 모양이다. 원은 투명하고 마이크는 흰색이다.
 */
@Composable
fun PickoMicBadge(
    modifier: Modifier = Modifier,
    onDim: Boolean = false,
) {
    val colors = PickoTheme.colors
    val rotation by rememberInfiniteTransition(label = "micHalo").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(HaloRotationMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "micHaloRotation",
    )

    Box(
        modifier = modifier
            .drawBehind { drawAuroraHalo(rotation) }
            .size(MicCircleSize)
            .then(if (onDim) Modifier else Modifier.background(colors.glassSurface, CircleShape)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PickoIcons.Microphone,
            contentDescription = null,
            modifier = Modifier.size(width = 62.dp, height = 93.dp),
            tint = if (onDim) colors.white else colors.micIcon,
        )
    }
}

private fun DrawScope.drawAuroraHalo(rotationDegrees: Float) {
    val brush = Brush.sweepGradient(colorStops = HaloStops, center = center)

    rotate(degrees = rotationDegrees, pivot = center) {
        drawIntoCanvas { canvas ->
            HaloLayers.forEach { layer ->
                val paint = Paint().apply {
                    style = PaintingStyle.Stroke
                    strokeWidth = layer.strokeWidth.dp.toPx()
                    isAntiAlias = true
                    brush.applyTo(size, this, layer.alpha)
                    if (layer.blurSigma > 0f) {
                        asFrameworkPaint().maskFilter =
                            BlurMaskFilter(layer.blurSigma.dp.toPx() * BlurRadiusPerSigma, BlurMaskFilter.Blur.NORMAL)
                    }
                }
                canvas.drawCircle(center, layer.radius.dp.toPx(), paint)
            }
        }
    }
}

@PickoCarPreview
@Composable
private fun PickoMicBadgePreview() {
    PickoPreviewSurface {
        PickoMicBadge()
    }
}
