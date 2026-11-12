package com.offcourse.picko.core.designsystem.component.indicator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme

/** 음성을 기다리고 있음을 알리는 원형 마이크 표시 */
@Composable
fun PickoMicBadge(modifier: Modifier = Modifier) {
    val colors = PickoTheme.colors

    Box(
        modifier = modifier
            .size(160.dp)
            .background(colors.glassSurface, CircleShape)
            .border(4.dp, colors.accentCornflower, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PickoIcons.Microphone,
            contentDescription = null,
            modifier = Modifier.size(width = 62.dp, height = 93.dp),
            tint = colors.micIcon,
        )
    }
}

@PickoCarPreview
@Composable
private fun PickoMicBadgePreview() {
    PickoPreviewSurface {
        PickoMicBadge()
    }
}
