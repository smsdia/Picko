package com.offcourse.picko.data.route.repository

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.route.model.DestinationRoute
import com.offcourse.picko.data.route.model.PickupRoutes

interface RouteRepository {
    suspend fun findPickupRoutes(
        vehicle: Coordinate,
        passenger: Coordinate,
        pickup: Coordinate,
    ): PickupRoutes

    /** 픽업을 마친 [from]에서 목적지까지의 경로 */
    suspend fun findDestinationRoute(from: Coordinate): DestinationRoute
}
