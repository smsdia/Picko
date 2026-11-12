package com.offcourse.picko.data.route.model

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.model.Coordinate
import kotlinx.collections.immutable.ImmutableList

/** 지도에 선으로 그릴 경로. [points]는 출발지부터 도착지까지 순서대로 이어진다. */
@Immutable
data class RouteModel(
    val points: ImmutableList<Coordinate>,
    val durationMinutes: Int,
)

/** 픽업 지점으로 가는 차량 경로와 탑승자의 도보 경로. */
@Immutable
data class PickupRoutes(
    val vehicle: RouteModel,
    val passengerWalk: RouteModel,
)

/** 픽업을 마친 뒤 목적지까지의 경로. */
@Immutable
data class DestinationRoute(
    val destinationName: String,
    val route: RouteModel,
    val distanceKm: Int,
)
