package com.offcourse.picko.presentation.pickup.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.indicator.PickoMicBadge
import com.offcourse.picko.core.designsystem.theme.PickoTheme

/**
 * 연락처나 장소를 못 찾았거나 다시 말하려 할 때 화면을 덮는 음성 재요청 안내.
 * 새 Gleo 호출이 오면 흐름이 새로 시작되므로, 여기서는 안내만 보여 주고 뒤로 가기나 바깥 터치로 닫는다.
 */
@Composable
internal fun VoiceRetryOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors

    BackHandler(onBack = onDismiss)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.overlayDim)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PickoMicBadge()

            Text(
                text = stringResource(R.string.pickup_voice_retry_overlay),
                style = PickoTheme.typography.title.titleLarge,
                color = colors.white,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.home_voice_example),
                style = PickoTheme.typography.label.labelMedium,
                color = colors.white,
                textAlign = TextAlign.Center,
            )
        }
    }
}
