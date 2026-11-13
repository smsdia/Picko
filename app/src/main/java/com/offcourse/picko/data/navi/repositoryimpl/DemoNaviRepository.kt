package com.offcourse.picko.data.navi.repositoryimpl

import com.offcourse.picko.core.common.model.distanceMetersTo
import com.offcourse.picko.data.navi.model.NaviDestinationModel
import com.offcourse.picko.data.navi.model.NaviTripModel
import com.offcourse.picko.data.navi.repository.NaviRepository
import com.offcourse.picko.data.vehicle.repositoryimpl.DEMO_SPEED_METERS_PER_SECOND
import com.offcourse.picko.data.vehicle.repositoryimpl.DemoVehicleRepository
import javax.inject.Inject
import kotlin.math.ceil

/**
 * 시연용 내비. 에뮬레이터의 내비는 위치를 받지 않아 소요 시간이 줄지 않으므로, 시연 차량의 현재 위치에서
 * 목적지까지의 직선거리를 시연 속도로 나눠 시간을 돌려준다. 안내 시작은 실제 내비에 맡긴다.
 */
class DemoNaviRepository @Inject constructor(
    private val vehicle: DemoVehicleRepository,
    private val real: NaviHelperRepository,
) : NaviRepository {
    override suspend fun previewTrip(destination: NaviDestinationModel): NaviTripModel {
        val meters = vehicle.position.value.distanceMetersTo(destination.coordinate)

        return NaviTripModel(
            durationMinutes = ceil(meters / DEMO_SPEED_METERS_PER_SECOND / SECONDS_PER_MINUTE).toInt(),
            distanceMeters = meters.toInt(),
        )
    }

    override suspend fun startGuidance(destination: NaviDestinationModel): Boolean = real.startGuidance(destination)

    private companion object {
        const val SECONDS_PER_MINUTE = 60
    }
}
