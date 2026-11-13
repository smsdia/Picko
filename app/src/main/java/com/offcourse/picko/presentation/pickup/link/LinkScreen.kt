package com.offcourse.picko.presentation.pickup.link

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.card.PickoContactActionCard
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.pickup.LinkStatus
import com.offcourse.picko.presentation.pickup.PickupFlowState
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.LoadingContent
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects
import com.offcourse.picko.presentation.pickup.component.highlightedText

/** 탑승자에게 위치 공유 링크를 보낼지 확인하고, 보낸 뒤 위치가 오기를 기다리는 화면 */
@Composable
internal fun LinkRoute(
    viewModel: PickupFlowViewModel,
    onNavigate: (PickupStep) -> Unit,
    onFlowFinished: () -> Unit,
    onPickupRequested: (PickupRequestModel) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PickupFlowEffects(
        viewModel = viewModel,
        onNavigate = onNavigate,
        onFlowFinished = onFlowFinished,
        onPickupRequested = onPickupRequested,
    )

    LifecycleResumeEffect(Unit) {
        viewModel.onLinkScreenResumed()
        onPauseOrDispose { }
    }

    // 기다리는 중에 뒤로 가면 기다림을 멈추고 확인 화면으로 돌아간다.
    BackHandler(enabled = state.linkStatus != LinkStatus.Idle, onBack = viewModel::onLinkWaitCancelled)

    LinkScreen(
        state = state,
        onSendClick = viewModel::onSendLinkClicked,
    )
}

@Composable
private fun LinkScreen(
    state: PickupFlowState,
    onSendClick: () -> Unit,
) {
    val colors = PickoTheme.colors
    val contact = state.selectedContact
    val name = contact?.name ?: state.request.passenger

    if (state.linkStatus != LinkStatus.Idle) {
        PickoScreenFrame(showHeader = false) {
            LoadingContent(
                message = highlightedText(
                    template = stringResource(R.string.link_waiting, name),
                    highlight = name,
                    color = colors.accentText,
                ),
            )
        }
        return
    }

    PickoScreenFrame {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = highlightedText(
                            template = stringResource(R.string.link_title, name),
                            highlight = name,
                            color = colors.accentText,
                        ),
                        style = PickoTheme.typography.headline.headlineLarge,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.link_description),
                        // labelLarge의 줄 높이(24sp)는 글자(32)보다 작아 두 줄이 겹치므로 늘려 쓴다.
                        style = PickoTheme.typography.label.labelLarge.copy(lineHeight = 44.sp),
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }

                if (contact != null) {
                    PickoContactActionCard(
                        name = contact.name,
                        phone = contact.phoneNumber,
                        onSendClick = onSendClick,
                        modifier = Modifier.widthIn(max = 960.dp),
                    )
                }
            }
        }
    }
}

@PickoCarPreview
@Composable
private fun LinkScreenPreview() {
    PickoTheme {
        LinkScreen(
            state = PickupFlowState(
                request = PickupRequestModel(passenger = "현대"),
                selectedContact = ContactModel(name = "현대", phoneNumber = "010-1234-5678"),
            ),
            onSendClick = {},
        )
    }
}
