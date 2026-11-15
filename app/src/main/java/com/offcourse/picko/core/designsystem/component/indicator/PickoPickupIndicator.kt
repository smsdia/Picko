package com.offcourse.picko.core.designsystem.component.indicator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.core.common.extensions.dropShadowLevel1

/** 스마트 픽업이 켜져 있음을 알리는 작은 표시. 문구는 [text]로 받는다. */
@Composable
fun PickoPickupIndicator(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = modifier
            .dropShadowLevel1(shape)
            .clip(shape)
            .background(colors.glassSurface)
            .border(1.dp, colors.glassBorder, shape)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(colors.accentBright, CircleShape),
        )
        Text(
            text = text,
            style = PickoTheme.typography.tag.tagLabel,
            color = colors.textIndicator,
        )
    }
}

@PickoCarPreview
@Composable
private fun PickoPickupIndicatorPreview() {
    PickoPreviewSurface {
        PickoPickupIndicator(text = "SMART PICKUP")
    }
}
