package com.offcourse.picko.presentation.pickup.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.component.marker.PickoMapMarker
import com.offcourse.picko.core.designsystem.component.marker.PickoMapMarkerType
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.route.model.DestinationRoute
import com.offcourse.picko.data.route.model.RouteModel
import com.offcourse.picko.presentation.pickup.PickupFlowState
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.MapMarkerSlot
import com.offcourse.picko.presentation.pickup.component.MapRouteLine
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects
import com.offcourse.picko.presentation.pickup.component.RouteMap
import kotlinx.collections.immutable.persistentListOf

/** 픽업을 마친 뒤 목적지까지의 경로를 지도로 안내하는 화면. 시안이 없어 내비게이션 화면의 구성을 따른다. */
@Composable
internal fun GuideRoute(
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

    GuideScreen(
        state = state,
        onFinishClick = viewModel::onGuideFinished,
    )
}

@Composable
private fun GuideScreen(
    state: PickupFlowState,
    onFinishClick: () -> Unit,
) {
    val guide = state.destinationRoute

    PickoScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 29.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (guide != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(PickoTheme.colors.surfaceCard)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    GuideMap(
                        guide = guide,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                    )

                    GuideSummary(guide = guide)
                }
            }

            PickoButton(
                text = stringResource(R.string.guide_finish),
                onClick = onFinishClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GuideMap(
    guide: DestinationRoute,
    modifier: Modifier = Modifier,
) {
    var isMapFailed by remember { mutableStateOf(false) }

    if (isMapFailed) {
        Text(
            text = stringResource(R.string.main_map_error),
            modifier = modifier,
            style = PickoTheme.typography.label.labelLarge,
            color = PickoTheme.colors.textSecondary,
        )
        return
    }

    val points = guide.route.points
    val vehicleTitle = stringResource(R.string.marker_vehicle_title)
    val vehicleDescription = stringResource(R.string.marker_vehicle_description)
    val etaText = stringResource(R.string.pickup_eta_minutes, guide.route.durationMinutes)

    RouteMap(
        routes = listOf(MapRouteLine(points = points, color = PickoTheme.colors.accent)),
        markers = listOf(
            MapMarkerSlot(points.first()) {
                PickoMapMarker(type = PickoMapMarkerType.Driver, title = vehicleTitle, description = vehicleDescription)
            },
            MapMarkerSlot(points.last()) {
                PickoMapMarker(type = PickoMapMarkerType.Pickup, title = guide.destinationName, description = etaText)
            },
        ),
        onMapError = { isMapFailed = true },
        modifier = modifier,
    )
}

/** 목적지 이름, 도착까지 남은 시간, 남은 거리를 한 줄로 보여 주는 요약 */
@Composable
private fun GuideSummary(guide: DestinationRoute) {
    val colors = PickoTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surface)
            .padding(horizontal = 32.dp, vertical = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryItem(
            label = stringResource(R.string.guide_destination_label),
            value = guide.destinationName,
            isEmphasized = false,
        )
        SummaryItem(
            label = stringResource(R.string.guide_eta_label),
            value = stringResource(R.string.pickup_eta_minutes, guide.route.durationMinutes),
            isEmphasized = true,
        )
        SummaryItem(
            label = stringResource(R.string.guide_distance_label),
            value = stringResource(R.string.guide_distance_km, guide.distanceKm),
            isEmphasized = true,
        )
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    isEmphasized: Boolean,
) {
    val colors = PickoTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = PickoTheme.typography.label.labelSmall,
            color = colors.textSecondary,
        )
        Text(
            text = value,
            style = PickoTheme.typography.label.labelLargeBold,
            color = if (isEmphasized) colors.accentStrong else colors.textPrimary,
        )
    }
}

@PickoCarPreview
@Composable
private fun GuideScreenPreview() {
    PickoTheme {
        GuideScreen(
            state = PickupFlowState(
                request = PickupRequestModel(passenger = "현대"),
                destinationRoute = DestinationRoute(
                    destinationName = "선릉역",
                    route = RouteModel(
                        points = persistentListOf(Coordinate(37.4948, 127.0632), Coordinate(37.5045, 127.0490)),
                        durationMinutes = 12,
                    ),
                    distanceKm = 5,
                ),
            ),
            onFinishClick = {},
        )
    }
}
