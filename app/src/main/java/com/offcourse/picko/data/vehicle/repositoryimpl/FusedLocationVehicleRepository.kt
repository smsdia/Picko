package com.offcourse.picko.data.vehicle.repositoryimpl

import ai.pleos.playground.location.fused.FusedLocation
import ai.pleos.playground.location.fused.listener.FusedLocationListener
import ai.pleos.playground.location.fused.model.LocationData
import android.content.Context
import android.content.pm.PackageManager
import com.offcourse.picko.data.vehicle.model.VehicleLocation
import com.offcourse.picko.data.vehicle.model.VehiclePermission
import com.offcourse.picko.data.vehicle.repository.VehicleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

/**
 * Pleos FusedLocation SDK 로 차량 위치를 받는다. 수집을 시작할 때 연결하고 수집이 끝나면 해제한다.
 * 위치를 받을 수 없으면(미지원, 오류) 값을 내보내지 않고 끝난다. 호출하는 쪽이 시간 제한을 둔다.
 */
class FusedLocationVehicleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : VehicleRepository {
    override fun observeLocation(): Flow<VehicleLocation> = callbackFlow {
        // 권한이 없으면 SDK 가 SecurityException 을 던지므로 먼저 확인한다. 사용자가 허용하기 전에는 위치를 받을 수 없다.
        if (context.checkSelfPermission(VehiclePermission.LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("차량 위치 권한이 허용되지 않았다")
            close()
            return@callbackFlow
        }

        val fusedLocation = FusedLocation(context)

        if (!fusedLocation.isSupported()) {
            Timber.w("FusedLocation 을 지원하지 않는 기기다")
            close()
            return@callbackFlow
        }

        val listener = object : FusedLocationListener {
            override fun onChanged(location: LocationData?) {
                location?.let { trySend(it.toModel()) }
            }

            override fun onError(exception: Exception) {
                Timber.w(exception, "FusedLocation 오류")
                close()
            }
        }

        try {
            fusedLocation.initialize()
            fusedLocation.registerFusedLocationCallback(listener)
        } catch (e: Exception) {
            Timber.w(e, "FusedLocation 을 시작하지 못했다")
            runCatching { fusedLocation.release() }
            close()
            return@callbackFlow
        }

        awaitClose {
            runCatching {
                fusedLocation.unregisterFusedLocationCallback(listener)
                fusedLocation.release()
            }.onFailure { Timber.w(it, "FusedLocation 해제에 실패했다") }
        }
    }
}

// confidence 는 에뮬레이터에서 GPS 정확도(hAcc, 미터)와 같은 값이 와서 정확도로 본다.
private fun LocationData.toModel() = VehicleLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = confidence,
    timestampMillis = timestampMillis,
)
