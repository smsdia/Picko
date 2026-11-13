package com.offcourse.picko.data.route.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.route.datasource.KakaoDirectionsDataSource
import com.offcourse.picko.data.route.dto.toRouteModel
import com.offcourse.picko.data.route.model.DestinationRoute
import com.offcourse.picko.data.route.model.PickupRoutes
import com.offcourse.picko.data.route.repository.RouteRepository
import javax.inject.Inject

/**
 * 차량 경로는 카카오모빌리티 자동차 길찾기로 구한다. 도보 경로는 도보 길찾기가 제휴 파트너 전용이라
 * 아직 [FakeRouteRepository]의 값을 쓰고, 호출이 실패해도 차량 경로를 같은 가짜 값으로 대신한다.
 */
class KakaoRouteRepository @Inject constructor(
    private val directions: KakaoDirectionsDataSource,
    private val fallback: FakeRouteRepository,
) : RouteRepository {
    override suspend fun findPickupRoutes(
        vehicle: Coordinate,
        passenger: Coordinate,
        pickup: Coordinate,
    ): PickupRoutes {
        val fake = fallback.findPickupRoutes(vehicle, passenger, pickup)
        val carRoute = directions.findCarRoute(origin = vehicle, destination = pickup)?.toRouteModel()
            ?: return fake

        return fake.copy(vehicle = carRoute)
    }

    override suspend fun findDestinationRoute(from: Coordinate): DestinationRoute =
        fallback.findDestinationRoute(from)
}
