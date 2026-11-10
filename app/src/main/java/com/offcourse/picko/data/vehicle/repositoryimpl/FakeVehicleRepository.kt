package com.offcourse.picko.data.vehicle.repositoryimpl

import com.offcourse.picko.data.vehicle.model.VehicleLocation
import com.offcourse.picko.data.vehicle.repository.VehicleRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Pleos SDK 나 에뮬레이터가 없는 환경에서 쓰는 가짜 구현. 고정 좌표를 주기적으로 내보낸다. */
class FakeVehicleRepository @Inject constructor() : VehicleRepository {
    override fun observeLocation(): Flow<VehicleLocation> = flow {
        while (true) {
            emit(
                VehicleLocation(
                    latitude = FAKE_LATITUDE,
                    longitude = FAKE_LONGITUDE,
                    accuracyMeters = FAKE_ACCURACY_METERS,
                    timestampMillis = System.currentTimeMillis(),
                ),
            )
            delay(EMIT_INTERVAL_MILLIS)
        }
    }

    private companion object {
        const val FAKE_LATITUDE = 37.4979
        const val FAKE_LONGITUDE = 127.0276
        const val FAKE_ACCURACY_METERS = 5f
        const val EMIT_INTERVAL_MILLIS = 1_000L
    }
}
