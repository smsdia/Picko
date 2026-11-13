package com.offcourse.picko.domain.pickup.usecase

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.navi.model.NaviDestinationModel
import com.offcourse.picko.data.navi.repository.NaviRepository
import com.offcourse.picko.data.route.model.PickupRoutes
import com.offcourse.picko.data.route.repository.RouteRepository
import javax.inject.Inject

/**
 * 픽업 지점으로 가는 경로를 만든다. 지도에 그릴 선과 도보 경로는 [RouteRepository]에서 얻고,
 * 차량의 소요 시간은 차량 내비가 계산한 값으로 바꾼다. 내비가 답하지 못하면 [RouteRepository]의 값을 그대로 쓴다.
 */
class FindPickupRoutesUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
    private val naviRepository: NaviRepository,
) {
    suspend operator fun invoke(
        vehicle: Coordinate,
        passenger: Coordinate,
        pickup: Coordinate,
        pickupName: String,
    ): PickupRoutes {
        val routes = routeRepository.findPickupRoutes(
            vehicle = vehicle,
            passenger = passenger,
            pickup = pickup,
        )
        val trip = naviRepository.previewTrip(NaviDestinationModel(name = pickupName, coordinate = pickup))
            ?: return routes

        return routes.copy(vehicle = routes.vehicle.copy(durationMinutes = trip.durationMinutes))
    }
}
