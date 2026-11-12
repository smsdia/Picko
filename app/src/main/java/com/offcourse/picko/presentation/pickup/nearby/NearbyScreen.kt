package com.offcourse.picko.presentation.pickup.nearby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.button.PickoButtonType
import com.offcourse.picko.core.designsystem.component.card.PickoNearbyPlaceResultCard
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.place.model.PickupReason
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.presentation.pickup.PickupFlowState
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects
import com.offcourse.picko.presentation.pickup.component.PickupHeading
import com.offcourse.picko.presentation.pickup.component.VoiceRetryOverlay
import kotlinx.collections.immutable.persistentListOf

/** 탑승자 위치를 반영해 추천한 안전한 정차 장소를 고르는 화면 */
@Composable
internal fun NearbyRoute(
    viewModel: PickupFlowViewModel,
    onNavigate: (PickupStep) -> Unit,
    onFlowFinished: () -> Unit,
    onPickupRequested: (PickupRequestModel) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PickupFlowEffects(
        viewModel = viewModel,
        onNavigate = onNavigate,
        onFlowFinished = onFlowFinished,
        onPickupRequested = onPickupRequested,
    )

    NearbyScreen(
        state = state,
        onPlaceClick = viewModel::onNearbySelected,
        onBackClick = onBack,
        onSpeakAgainClick = viewModel::onVoiceRetryRequested,
        onVoiceRetryMicClick = viewModel::onVoiceRetryMicClicked,
        onVoiceAnnounce = viewModel::onVoiceAnnounce,
        onVoiceRetryDismiss = viewModel::onVoiceRetryDismissed,
    )
}

@Composable
private fun NearbyScreen(
    state: PickupFlowState,
    onPlaceClick: (PlaceModel) -> Unit,
    onBackClick: () -> Unit,
    onSpeakAgainClick: () -> Unit,
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
                title = stringResource(R.string.nearby_title),
                subtitle = stringResource(R.string.nearby_subtitle),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(state.nearbyCandidates, key = { _, place -> place.id }) { index, place ->
                    PickoNearbyPlaceResultCard(
                        order = index + 1,
                        name = place.name,
                        distanceText = stringResource(R.string.pickup_meta_distance, place.distanceKm),
                        durationText = stringResource(R.string.pickup_meta_duration, place.durationMinutes),
                        recommendation = reasonText(place.reason),
                        isSelected = false,
                        onClick = { onPlaceClick(place) },
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PickoButton(
                    text = stringResource(R.string.pickup_back),
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                )
                PickoButton(
                    text = stringResource(R.string.pickup_speak_again),
                    onClick = onSpeakAgainClick,
                    modifier = Modifier.weight(1f),
                    type = PickoButtonType.Secondary,
                )
            }
        }
    }
}

@Composable
private fun reasonText(reason: PickupReason?): String = when (reason) {
    is PickupReason.ShortWalk -> stringResource(R.string.pickup_reason_short_walk, reason.minutes)
    PickupReason.NearEntrance -> stringResource(R.string.pickup_reason_near_entrance)
    PickupReason.EasyStop -> stringResource(R.string.pickup_reason_easy_stop)
    null -> ""
}

@PickoCarPreview
@Composable
private fun NearbyScreenPreview() {
    PickoTheme {
        NearbyScreen(
            state = PickupFlowState(
                request = PickupRequestModel(passenger = "현대"),
                nearbyCandidates = persistentListOf(
                    PlaceModel(
                        id = "1",
                        name = "대치역 2번 출구 앞 안전정차구역",
                        coordinate = Coordinate(37.4948, 127.0632),
                        distanceKm = 18,
                        durationMinutes = 20,
                        reason = PickupReason.ShortWalk(minutes = 1),
                    ),
                    PlaceModel(
                        id = "2",
                        name = "대치동 학원가 갓길 정류장",
                        coordinate = Coordinate(37.4951, 127.0578),
                        distanceKm = 18,
                        durationMinutes = 20,
                        reason = PickupReason.NearEntrance,
                    ),
                ),
            ),
            onPlaceClick = {},
            onBackClick = {},
            onSpeakAgainClick = {},
            onVoiceRetryMicClick = {},
            onVoiceAnnounce = {},
            onVoiceRetryDismiss = {},
        )
    }
}
