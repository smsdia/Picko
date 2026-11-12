package com.offcourse.picko.presentation.pickup.complete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.button.PickoButtonType
import com.offcourse.picko.core.designsystem.component.indicator.PickoSuccessEmblem
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects

/** 픽업을 마쳤음을 알리고 목적지 안내로 이어 주는 화면 */
@Composable
internal fun CompleteRoute(
    viewModel: PickupFlowViewModel,
    onNavigate: (PickupStep) -> Unit,
    onFlowFinished: () -> Unit,
) {
    PickupFlowEffects(
        viewModel = viewModel,
        onNavigate = onNavigate,
        onFlowFinished = onFlowFinished,
    )

    CompleteScreen(
        onGuideClick = viewModel::onGuideClicked,
        onMusicClick = viewModel::onMusicClicked,
    )
}

@Composable
private fun CompleteScreen(
    onGuideClick: () -> Unit,
    onMusicClick: () -> Unit,
) {
    val colors = PickoTheme.colors

    PickoScreenFrame {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                PickoSuccessEmblem()

                Text(
                    text = stringResource(R.string.complete_title),
                    style = PickoTheme.typography.headline.headlineLarge,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.complete_subtitle),
                    style = PickoTheme.typography.label.labelLarge,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )

                Row(
                    modifier = Modifier.widthIn(max = 960.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    PickoButton(
                        text = stringResource(R.string.complete_guide),
                        onClick = onGuideClick,
                        modifier = Modifier.weight(1f),
                    )
                    PickoButton(
                        text = stringResource(R.string.complete_music),
                        onClick = onMusicClick,
                        modifier = Modifier.weight(1f),
                        type = PickoButtonType.Secondary,
                    )
                }
            }
        }
    }
}

@PickoCarPreview
@Composable
private fun CompleteScreenPreview() {
    PickoTheme {
        CompleteScreen(
            onGuideClick = {},
            onMusicClick = {},
        )
    }
}
