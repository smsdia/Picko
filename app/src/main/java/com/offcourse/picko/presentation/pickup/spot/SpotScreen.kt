package com.offcourse.picko.presentation.pickup.spot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.offcourse.picko.core.designsystem.component.card.PickoPickupInfoCard
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

/** 추천된 픽업 지점을 지도와 도착 시간으로 보여 주고 내비게이션 안내를 시작하는 화면 */
@Composable
internal fun SpotRoute(
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

    SpotScreen(
        state = state,
        onStartNavigationClick = viewModel::onStartNavigationClicked,
    )
}

@Composable
private fun SpotScreen(
    state: PickupFlowState,
    onStartNavigationClick: () -> Unit,
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

                    PickoPickupInfoCard(
                        locationName = spot.name,
                        vehicleEta = stringResource(R.string.pickup_eta_minutes, routes.vehicle.durationMinutes),
                        pedestrianEta = stringResource(R.string.pickup_eta_minutes, routes.passengerWalk.durationMinutes),
                        reason = stringResource(R.string.spot_reason),
                    )
                }
            }

            PickoButton(
                text = stringResource(R.string.spot_start_navigation),
                onClick = onStartNavigationClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PickoCarPreview
@Composable
private fun SpotScreenPreview() {
    PickoTheme {
        SpotScreen(
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
            onStartNavigationClick = {},
        )
    }
}
