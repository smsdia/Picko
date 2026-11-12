package com.offcourse.picko.core.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.common.extensions.dropShadowLevel1
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.theme.PickoTheme

enum class PickoButtonType { Primary, Secondary }

// Figma에 비활성 상태 시안이 없어서 투명도로만 구분한다. TODO(figma): 시안이 생기면 맞춘다.
private const val DisabledAlpha = 0.4f

@Composable
fun PickoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: PickoButtonType = PickoButtonType.Primary,
    isEnabled: Boolean = true,
) {
    val colors = PickoTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val (background, borderColor, contentColor) = when (type) {
        PickoButtonType.Primary -> Triple(colors.buttonPrimary, colors.ctaBorder, colors.onAccent)
        PickoButtonType.Secondary -> Triple(colors.buttonSecondary, colors.buttonSecondaryBorder, colors.textPrimary)
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 80.dp)
            .alpha(if (isEnabled) 1f else DisabledAlpha)
            .dropShadowLevel1(shape)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(
                enabled = isEnabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = PickoTheme.typography.label.labelLargeBold,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@PickoCarPreview
@Composable
private fun PickoButtonPreview() {
    PickoPreviewSurface {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PickoButton(
                text = "목적지 안내하기",
                onClick = {},
            )
            PickoButton(
                text = "음악 재생하기",
                onClick = {},
                type = PickoButtonType.Secondary,
            )
        }
    }
}
