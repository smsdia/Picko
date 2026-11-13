package com.offcourse.picko.data.vehicle.repository

import com.offcourse.picko.data.vehicle.model.VehicleLocation
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    /** 차량 위치를 실시간으로 내보낸다. */
    fun observeLocation(): Flow<VehicleLocation>

    /** 운전자가 픽업 지점으로 내비게이션 안내를 시작했다. 안내를 시작해야 움직이는 시연 차량만 쓰고, 나머지는 할 일이 없다. */
    fun onNavigationStarted() = Unit
}
