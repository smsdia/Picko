package com.offcourse.picko.presentation.pickup.driving

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.button.PickoButtonType
import com.offcourse.picko.core.designsystem.component.card.PickoNavigationStatusCard
import com.offcourse.picko.core.designsystem.component.dialog.PickoDialog
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.route.model.PickupRoutes
import com.offcourse.picko.data.route.model.RouteModel
import com.offcourse.picko.presentation.pickup.PickupFlowState
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects
import com.offcourse.picko.presentation.pickup.component.PickupMap
import kotlinx.collections.immutable.persistentListOf

/** 픽업 지점으로 이동하는 동안의 내비게이션 화면. 픽업을 마치거나 취소를 요청한다. */
@Composable
fun DrivingRoute(
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

    // 실수로 흐름을 벗어나지 않도록 뒤로 가면 취소 여부를 먼저 묻는다.
    BackHandler(enabled = !state.isCancelDialogVisible, onBack = viewModel::onCancelRequested)

    DrivingScreen(
        state = state,
        onPickupCompleteClick = viewModel::onPickupCompleted,
        onCancelRequestClick = viewModel::onCancelRequested,
        onCancelContinueClick = viewModel::onCancelContinued,
        onCancelConfirmClick = viewModel::onCancelConfirmed,
    )
}

@Composable
private fun DrivingScreen(
    state: PickupFlowState,
    onPickupCompleteClick: () -> Unit,
    onCancelRequestClick: () -> Unit,
    onCancelContinueClick: () -> Unit,
    onCancelConfirmClick: () -> Unit,
) {
    val spot = state.selectedPlace
    val routes = state.routes

    PickoScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 29.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (spot != null && routes != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(PickoTheme.colors.surfaceCard)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    PickupMap(
                        spot = spot,
                        routes = routes,
                        vehicle = state.vehicle,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                    )

                    PickoNavigationStatusCard(
                        locationName = spot.name,
                        vehicleEta = stringResource(R.string.pickup_eta_minutes, routes.vehicle.durationMinutes),
                        pedestrianEta = stringResource(R.string.pickup_eta_minutes, routes.passengerWalk.durationMinutes),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PickoButton(
                    text = stringResource(R.string.navigation_complete),
                    onClick = onPickupCompleteClick,
                    modifier = Modifier.weight(1f),
                )
                PickoButton(
                    text = stringResource(R.string.navigation_cancel_request),
                    onClick = onCancelRequestClick,
                    modifier = Modifier.weight(1f),
                    type = PickoButtonType.Secondary,
                )
            }
        }
    }

    if (state.isCancelDialogVisible) {
        PickoDialog(
            title = stringResource(R.string.cancel_dialog_title),
            description = stringResource(R.string.cancel_dialog_description),
            primaryText = stringResource(R.string.cancel_dialog_continue),
            onPrimaryClick = onCancelContinueClick,
            secondaryText = stringResource(R.string.cancel_dialog_confirm),
            onSecondaryClick = onCancelConfirmClick,
            onDismissRequest = onCancelContinueClick,
        )
    }
}

@PickoCarPreview
@Composable
private fun DrivingScreenPreview() {
    PickoTheme {
        DrivingScreen(
            state = PickupFlowState(
                request = PickupRequestModel(passenger = "현대"),
                selectedPlace = PlaceModel(
                    id = "1",
                    name = "대치역 2번 출구 앞",
                    coordinate = Coordinate(37.4948, 127.0632),
                    distanceKm = 18,
                    durationMinutes = 20,
                ),
                routes = PickupRoutes(
                    vehicle = RouteModel(
                        points = persistentListOf(Coordinate(37.4979, 127.0276), Coordinate(37.4948, 127.0632)),
                        durationMinutes = 3,
                    ),
                    passengerWalk = RouteModel(
                        points = persistentListOf(Coordinate(37.4952, 127.0641), Coordinate(37.4948, 127.0632)),
                        durationMinutes = 2,
                    ),
                ),
            ),
            onPickupCompleteClick = {},
            onCancelRequestClick = {},
            onCancelContinueClick = {},
            onCancelConfirmClick = {},
        )
    }
}
