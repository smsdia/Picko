package com.offcourse.picko.presentation.pickup.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.marker.PickoMapMarker
import com.offcourse.picko.core.designsystem.component.marker.PickoMapMarkerType
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.route.model.PickupRoutes

/** 차량, 탑승자, 픽업 지점 마커와 두 경로를 그린 지도. 픽업 지점 추천과 내비게이션 화면이 함께 쓴다. */
@Composable
internal fun PickupMap(
    spot: PlaceModel,
    routes: PickupRoutes,
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

    val colors = PickoTheme.colors
    val vehicleStart = routes.vehicle.points.first()
    val passengerStart = routes.passengerWalk.points.first()
    val vehicleTitle = stringResource(R.string.marker_vehicle_title)
    val vehicleDescription = stringResource(R.string.marker_vehicle_description)
    val passengerTitle = stringResource(R.string.marker_passenger_title)
    val passengerDescription = stringResource(R.string.marker_passenger_description)
    val pickupTitle = stringResource(R.string.marker_pickup_title)
    val pickupDescription = stringResource(R.string.marker_pickup_description, routes.passengerWalk.durationMinutes)

    RouteMap(
        routes = listOf(
            MapRouteLine(points = routes.vehicle.points, color = colors.accent),
            MapRouteLine(points = routes.passengerWalk.points, color = colors.accentBright),
        ),
        markers = listOf(
            MapMarkerSlot(vehicleStart) {
                PickoMapMarker(type = PickoMapMarkerType.Driver, title = vehicleTitle, description = vehicleDescription)
            },
            MapMarkerSlot(passengerStart) {
                PickoMapMarker(type = PickoMapMarkerType.Pedestrian, title = passengerTitle, description = passengerDescription)
            },
            MapMarkerSlot(spot.coordinate) {
                PickoMapMarker(type = PickoMapMarkerType.Pickup, title = pickupTitle, description = pickupDescription)
            },
        ),
        onMapError = { isMapFailed = true },
        modifier = modifier,
    )
}
