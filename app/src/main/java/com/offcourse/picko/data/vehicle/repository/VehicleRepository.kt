package com.offcourse.picko.data.vehicle.repository

import android.content.Context
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.vehicle.model.VehicleLocation
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    /**
     * 차량 위치를 실시간으로 내보낸다.
     * 위치를 받을 수 없으면 값 없이 `VehicleLocationException`으로 끝난다.
     */
    fun observeLocation(): Flow<VehicleLocation>

    /**
     * 차량의 개인정보 보호 설정으로 위치가 막혔을 때 시스템이 띄우는 안내 팝업을 연다.
     * 팝업을 여는 데 Activity 를 쓰므로 [activityContext]는 Activity 컨텍스트여야 한다. 위치 접근을 허용해 주지는 않는다.
     */
    fun showLocationAccessBlockedPopup(activityContext: Context) = Unit

    /** 운전자가 [destination](픽업 지점)으로 내비게이션 안내를 시작했다. 안내를 시작해야 움직이는 시연 차량만 쓰고, 나머지는 할 일이 없다. */
    fun onNavigationStarted(destination: Coordinate) = Unit
}
