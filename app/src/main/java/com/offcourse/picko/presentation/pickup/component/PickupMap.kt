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
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.designsystem.component.marker.PickoMapMarkerBadge
import com.offcourse.picko.core.designsystem.component.marker.PickoMapMarkerLabel
import com.offcourse.picko.core.designsystem.component.marker.PickoMapMarkerType
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.route.model.PickupRoutes

/** 차량, 탑승자, 픽업 지점 마커와 두 경로를 그린 지도. 픽업 지점 추천과 내비게이션 화면이 함께 쓴다. */
@Composable
fun PickupMap(
    spot: PlaceModel,
    routes: PickupRoutes,
    modifier: Modifier = Modifier,
    // 차량의 지금 위치. 아직 없으면 경로의 출발점을 쓴다.
    vehicle: Coordinate? = null,
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
    val vehiclePosition = vehicle ?: routes.vehicle.points.first()
    val passengerStart = routes.passengerWalk.points.first()
    val vehicleTitle = stringResource(R.string.marker_vehicle_title)
    val vehicleDescription = stringResource(R.string.marker_vehicle_description)
    val passengerTitle = stringResource(R.string.marker_passenger_title)
    val passengerDescription = stringResource(R.string.marker_passenger_description)
    val pickupTitle = stringResource(R.string.marker_pickup_title)
    val pickupDescription = stringResource(R.string.marker_pickup_description, routes.passengerWalk.durationMinutes)

    // 도보 길찾기가 제휴 전용이라 탑승자의 도보 경로선은 그리지 않고 마커만 둔다. "도보 약 N분"은 직선거리 ÷ 보행 속도로 추정한 값이다.
    RouteMap(
        routes = listOf(
            MapRouteLine(points = routes.vehicle.points, color = colors.accent, progressFrom = vehiclePosition),
        ),
        // 라벨 자리가 모자라면 픽업 지점, 차량, 탑승자 순으로 먼저 얻는다.
        markers = listOf(
            MapMarkerSlot(
                coordinate = vehiclePosition,
                priority = 1,
                badge = { PickoMapMarkerBadge(type = PickoMapMarkerType.Driver) },
                label = { PickoMapMarkerLabel(title = vehicleTitle, description = vehicleDescription) },
            ),
            MapMarkerSlot(
                coordinate = passengerStart,
                priority = 2,
                badge = { PickoMapMarkerBadge(type = PickoMapMarkerType.Pedestrian) },
                label = { PickoMapMarkerLabel(title = passengerTitle, description = passengerDescription) },
            ),
            MapMarkerSlot(
                coordinate = spot.coordinate,
                priority = 0,
                badge = { PickoMapMarkerBadge(type = PickoMapMarkerType.Pickup) },
                label = { PickoMapMarkerLabel(title = pickupTitle, description = pickupDescription) },
            ),
        ),
        onMapError = { isMapFailed = true },
        modifier = modifier,
    )
}
