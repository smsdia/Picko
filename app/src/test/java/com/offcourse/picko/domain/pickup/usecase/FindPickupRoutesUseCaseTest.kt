package com.offcourse.picko.domain.pickup.usecase

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.navi.model.NaviDestinationModel
import com.offcourse.picko.data.navi.model.NaviGuidanceResult
import com.offcourse.picko.data.navi.model.NaviTripModel
import com.offcourse.picko.data.navi.repository.NaviRepository
import com.offcourse.picko.data.route.model.DestinationRoute
import com.offcourse.picko.data.route.model.PickupRoutes
import com.offcourse.picko.data.route.model.RouteModel
import com.offcourse.picko.data.route.repository.RouteRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FindPickupRoutesUseCaseTest {
    private val vehicle = Coordinate(37.50, 127.04)
    private val passenger = Coordinate(37.49, 127.05)
    private val pickup = Coordinate(37.4946, 127.0576)

    private val fakeRoutes = PickupRoutes(
        vehicle = RouteModel(points = persistentListOf(vehicle, pickup), durationMinutes = 3),
        passengerWalk = RouteModel(points = persistentListOf(passenger, pickup), durationMinutes = 2),
    )

    private val routeRepository = object : RouteRepository {
        override suspend fun findPickupRoutes(
            vehicle: Coordinate,
            passenger: Coordinate,
            pickup: Coordinate,
        ): PickupRoutes = fakeRoutes

        override suspend fun findDestinationRoute(from: Coordinate): DestinationRoute = error("쓰지 않는다")
    }

    private fun naviRepository(trip: NaviTripModel?, onPreview: (NaviDestinationModel) -> Unit = {}) =
        object : NaviRepository {
            override suspend fun previewTrip(destination: NaviDestinationModel): NaviTripModel? {
                onPreview(destination)
                return trip
            }

            override suspend fun startGuidance(destination: NaviDestinationModel): NaviGuidanceResult =
                error("쓰지 않는다")

            override suspend fun showNavigationScreen(): NaviGuidanceResult = error("쓰지 않는다")
        }

    private suspend fun FindPickupRoutesUseCase.find() = invoke(
        vehicle = vehicle,
        passenger = passenger,
        pickup = pickup,
        pickupName = "대치역",
    )

    @Test
    fun 내비가_소요_시간을_알려_주면_차량_소요_시간만_바꾼다() = runBlocking {
        val routes = FindPickupRoutesUseCase(routeRepository, naviRepository(NaviTripModel(40, 12_463))).find()

        assertEquals(40, routes.vehicle.durationMinutes)
        assertEquals(fakeRoutes.vehicle.points, routes.vehicle.points)
        assertEquals(fakeRoutes.passengerWalk, routes.passengerWalk)
    }

    @Test
    fun 내비가_답하지_못하면_경로_저장소의_값을_그대로_쓴다() = runBlocking {
        val routes = FindPickupRoutesUseCase(routeRepository, naviRepository(trip = null)).find()

        assertEquals(fakeRoutes, routes)
    }

    @Test
    fun 내비에는_픽업_지점의_이름과_좌표를_목적지로_묻는다() = runBlocking {
        var asked: NaviDestinationModel? = null

        FindPickupRoutesUseCase(routeRepository, naviRepository(null) { asked = it }).find()

        assertEquals(NaviDestinationModel(name = "대치역", coordinate = pickup), asked)
    }
}
