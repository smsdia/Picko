package com.offcourse.picko.data.route.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.route.model.DestinationRoute
import com.offcourse.picko.data.route.model.PickupRoutes
import com.offcourse.picko.data.route.model.RouteModel
import com.offcourse.picko.data.route.repository.RouteRepository
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf

/**
 * 길찾기 연동이 정해지기 전에 쓰는 가짜 구현.
 * 출발지와 도착지를 직각으로 꺾어 잇는 경로와 시안의 소요 시간을 돌려준다.
 */
class FakeRouteRepository @Inject constructor() : RouteRepository {
    override suspend fun findPickupRoutes(
        vehicle: Coordinate,
        passenger: Coordinate,
        pickup: Coordinate,
    ): PickupRoutes = PickupRoutes(
        vehicle = RouteModel(
            points = persistentListOf(vehicle, cornerOf(vehicle, pickup), pickup),
            durationMinutes = VEHICLE_ETA_MINUTES,
        ),
        passengerWalk = RouteModel(
            points = persistentListOf(passenger, cornerOf(passenger, pickup), pickup),
            durationMinutes = PASSENGER_WALK_MINUTES,
        ),
    )

    override suspend fun findDestinationRoute(from: Coordinate): DestinationRoute = DestinationRoute(
        destinationName = DESTINATION_NAME,
        route = RouteModel(
            points = persistentListOf(from, cornerOf(from, DESTINATION), DESTINATION),
            durationMinutes = DESTINATION_ETA_MINUTES,
        ),
        distanceKm = DESTINATION_DISTANCE_KM,
    )

    // 위도를 먼저 맞추고 경도를 맞추는 모서리. 지도에서 꺾인 선으로 보이게 한다.
    private fun cornerOf(from: Coordinate, to: Coordinate) = Coordinate(
        latitude = to.latitude,
        longitude = from.longitude,
    )

    private companion object {
        const val VEHICLE_ETA_MINUTES = 3
        const val PASSENGER_WALK_MINUTES = 2
        const val DESTINATION_ETA_MINUTES = 12
        const val DESTINATION_DISTANCE_KM = 5
        const val DESTINATION_NAME = "선릉역"
        val DESTINATION = Coordinate(latitude = 37.5045, longitude = 127.0490)
    }
}
