package com.offcourse.picko.presentation.pickup.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.common.util.UiState
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.card.PickoContactResultCard
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.pickup.PickupFlowState
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects
import com.offcourse.picko.presentation.pickup.component.PickupHeading
import com.offcourse.picko.presentation.pickup.component.VoiceRetryOverlay
import kotlinx.collections.immutable.persistentListOf

/** 같은 이름의 연락처가 여럿일 때 보이는 화면 */
@Composable
internal fun ContactRoute(
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

    ContactScreen(
        state = state,
        onContactClick = viewModel::onContactSelected,
        onVoiceRetryClick = viewModel::onVoiceRetryRequested,
        onVoiceRetryMicClick = viewModel::onVoiceRetryMicClicked,
        onVoiceAnnounce = viewModel::onVoiceAnnounce,
        onVoiceRetryDismiss = viewModel::onVoiceRetryDismissed,
    )
}

@Composable
private fun ContactScreen(
    state: PickupFlowState,
    onContactClick: (ContactModel) -> Unit,
    onVoiceRetryClick: () -> Unit,
    onVoiceRetryMicClick: () -> Unit,
    onVoiceAnnounce: (String) -> Unit,
    onVoiceRetryDismiss: () -> Unit,
) {
    PickoScreenFrame(
        overlay = {
            if (state.isVoiceRetryVisible) {
                VoiceRetryOverlay(
                    voice = state.voice,
                    onMicClick = onVoiceRetryMicClick,
                    onAnnounce = onVoiceAnnounce,
                    onDismiss = onVoiceRetryDismiss,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 29.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            PickupHeading(
                title = stringResource(R.string.contact_result_title),
                subtitle = stringResource(R.string.contact_result_subtitle),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(state.contactCandidates, key = { _, contact -> contact.phoneNumber }) { index, contact ->
                    PickoContactResultCard(
                        order = index + 1,
                        name = contact.name,
                        phone = contact.phoneNumber,
                        isSelected = false,
                        onClick = { onContactClick(contact) },
                    )
                }
            }

            PickoButton(
                text = stringResource(R.string.pickup_voice_retry),
                onClick = onVoiceRetryClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PickoCarPreview
@Composable
private fun ContactScreenPreview() {
    PickoTheme {
        ContactScreen(
            state = PickupFlowState(
                request = PickupRequestModel(passenger = "현대"),
                contactLookup = UiState.Success(
                    ContactLookupResult.Multiple(
                        persistentListOf(
                            ContactModel(name = "사랑하는 우리 현대", phoneNumber = "010-1234-5678"),
                            ContactModel(name = "현대 대리님", phoneNumber = "010-0000-0000"),
                            ContactModel(name = "현대 (회사)", phoneNumber = "010-1234-1234"),
                        ),
                    ),
                ),
            ),
            onContactClick = {},
            onVoiceRetryClick = {},
            onVoiceRetryMicClick = {},
            onVoiceAnnounce = {},
            onVoiceRetryDismiss = {},
        )
    }
}
