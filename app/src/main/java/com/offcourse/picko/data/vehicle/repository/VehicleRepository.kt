package com.offcourse.picko.data.vehicle.repository

import com.offcourse.picko.data.vehicle.model.VehicleLocation
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    /** 차량 위치를 실시간으로 내보낸다. */
    fun observeLocation(): Flow<VehicleLocation>
}
