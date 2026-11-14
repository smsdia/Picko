package com.offcourse.picko.presentation.pickup.complete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.button.PickoButtonType
import com.offcourse.picko.core.designsystem.component.indicator.PickoSuccessEmblem
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects
import com.offcourse.picko.presentation.pickup.component.PickupNoticeDialog

/** 픽업을 마쳤음을 알리고 목적지 안내로 이어 주는 화면 */
@Composable
fun CompleteRoute(
    viewModel: PickupFlowViewModel,
    onNavigate: (PickupStep) -> Unit,
    onFlowFinished: () -> Unit,
    onPickupRequested: (PickupRequestModel) -> Unit,
) {
    PickupFlowEffects(
        viewModel = viewModel,
        onNavigate = onNavigate,
        onFlowFinished = onFlowFinished,
        onPickupRequested = onPickupRequested,
    )

    val state by viewModel.state.collectAsStateWithLifecycle()

    CompleteScreen(
        onGuideClick = viewModel::onGuideClicked,
        onMusicClick = viewModel::onMusicClicked,
    )

    PickupNoticeDialog(notice = state.notice, viewModel = viewModel)
}

// Figma `PICKO / Pickup Complete / Content`(머리글 구분선 기준) 치수. 글자 상자 높이 차이는 줄 높이로 보정했다.
private val EmblemTopPadding = 156.dp
private val EmblemToTitle = 57.dp
private val TitleToSubtitle = 26.dp
private val SubtitleToActions = 79.dp
private val ActionsWidth = 900.dp

@Composable
private fun CompleteScreen(
    onGuideClick: () -> Unit,
    onMusicClick: () -> Unit,
) {
    val colors = PickoTheme.colors

    PickoScreenFrame {
        // Figma 시안은 머리글 구분선 아래에서 위부터 쌓는 배치라서, 가운데 정렬하지 않고 위쪽 간격을 직접 준다.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = EmblemTopPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PickoSuccessEmblem()

            Spacer(modifier = Modifier.height(EmblemToTitle))
            Text(
                text = stringResource(R.string.complete_title),
                style = PickoTheme.typography.headline.headlineLarge,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(TitleToSubtitle))
            Text(
                text = stringResource(R.string.complete_subtitle),
                // labelLarge의 줄 높이(24sp)가 글자(32)보다 작아 늘려 쓴다.
                style = PickoTheme.typography.label.labelLarge.copy(lineHeight = 44.sp),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(SubtitleToActions))
            Row(
                modifier = Modifier.width(ActionsWidth),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
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
