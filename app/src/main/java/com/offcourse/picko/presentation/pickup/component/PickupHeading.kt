package com.offcourse.picko.presentation.pickup.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.theme.PickoTheme

/** 선택 화면 위쪽의 제목과 안내 문구 */
@Composable
fun PickupHeading(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = PickoTheme.typography.headline.headlineLarge,
            color = PickoTheme.colors.textPrimary,
        )
        Text(
            text = subtitle,
            style = PickoTheme.typography.label.labelLarge,
            color = PickoTheme.colors.textSecondary,
        )
    }
}
