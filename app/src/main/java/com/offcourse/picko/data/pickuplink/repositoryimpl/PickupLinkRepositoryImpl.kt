package com.offcourse.picko.data.pickuplink.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.localstorage.session.SessionTokenStorage
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.pickuplink.datasource.PickoSessionDataSource
import com.offcourse.picko.data.pickuplink.datasource.PickoSessionSocket
import com.offcourse.picko.data.pickuplink.datasource.SocketEnd
import com.offcourse.picko.data.pickuplink.dto.CancelSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.CreateSessionRequestDto
import com.offcourse.picko.data.pickuplink.model.PickoServerException
import com.offcourse.picko.data.pickuplink.model.PickupSessionEndedException
import com.offcourse.picko.data.pickuplink.model.SmsNotSentException
import com.offcourse.picko.data.pickuplink.repository.PickupLinkRepository
import com.offcourse.picko.data.place.model.PlaceModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

// TODO(vehicle-info): 서버의 vehicleModel, vehiclePlate가 필수인데 Pleos SDK 공식 문서에서 차종과 번호판을 주는
//  API를 확인하지 못해 임시 고정값을 보낸다. `CarInfo.getCRNInfo`가 번호판인지는 문서에 없어 확인 전이다.
//  실제 값을 얻는 방법이 정해지면 바꾼다. 번호판은 개인정보라 저장하면 암호화한다.
private const val TEMPORARY_VEHICLE_MODEL = "현대 아반떼"
private const val TEMPORARY_VEHICLE_PLATE = "123가 4567"

private const val METERS_PER_KM = 1_000.0
private const val CANCEL_REASON = "driver_canceled"
private const val COMPLETE_REASON = "pickup_completed"
private const val SMS_FAILED_REASON = "sms_failed"
private const val ABANDON_REASON = "flow_abandoned"
private const val HTTP_SERVER_ERROR_MIN = 500

/**
 * 세션 만들기(문자 발송 포함)와 취소는 Picko 서버로 보낸다. 토큰은 [SessionTokenStorage]에 암호화해 두어
 * 앱이 꺼졌다 켜져도 취소할 수 있다.
 *
 * 탑승자 위치는 세션을 만든 뒤 WebSocket([PickoSessionSocket])으로 받는다. 차량 위치도 같은 연결로 보낸다.
 */
@Singleton
class PickupLinkRepositoryImpl @Inject constructor(
    private val dataSource: PickoSessionDataSource,
    private val tokenStorage: SessionTokenStorage,
    private val socket: PickoSessionSocket,
) : PickupLinkRepository {
    // 흐름을 맡은 ViewModel이 사라져도 세션 닫기를 끝낼 수 있게 앱 수명 동안 사는 범위를 따로 둔다.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var sessionPickup: Coordinate? = null

    override suspend fun sendLink(contact: ContactModel, pickup: PlaceModel) {
        // 이전 흐름이 취소되지 못한 채 남은 세션이 있으면 먼저 정리한다. 실패해도 새 세션 만들기는 막지 않는다.
        try {
            cancelSession()
        } catch (e: PickoServerException) {
            Timber.w(e, "남은 세션을 정리하지 못했다")
        }

        val response = dataSource.createSession(pickup.toRequest(contact))
        tokenStorage.save(response.token)
        sessionPickup = pickup.coordinate

        if (!response.smsSent) {
            // 탑승자에게 문자가 가지 않은 세션은 쓸 수 없다. 서버에 남기지 않고 바로 닫는다. 닫지 못해도 토큰이 남아 다음 전송 때 정리된다.
            try {
                closeSession(SMS_FAILED_REASON)
            } catch (e: PickoServerException) {
                Timber.w(e, "문자 발송에 실패한 세션을 닫지 못했다")
            }
            throw SmsNotSentException(response.smsDetail)
        }

        socket.connect(response.token)
    }

    override suspend fun awaitPassengerLocation(): Coordinate {
        // 위치가 오거나 서버가 세션을 끝낼 때까지 기다린다. 오래 걸리면 호출하는 쪽이 취소한다.
        val state = socket.state.first { it.pedestrian != null || it.end != null }
        state.end?.let { throw PickupSessionEndedException(isCanceled = it == SocketEnd.SessionCanceled) }

        return checkNotNull(state.pedestrian)
    }

    override fun observePassengerLocation(): Flow<Coordinate> {
        if (sessionPickup == null) return emptyFlow()

        return socket.state
            .map { it.pedestrian }
            .filterNotNull()
            .distinctUntilChanged()
    }

    override fun sendVehicleLocation(location: Coordinate) {
        if (sessionPickup == null) return

        socket.sendVehicleLocation(location)
    }

    override suspend fun cancelSession() = closeSession(CANCEL_REASON)

    override suspend fun completeSession() = closeSession(COMPLETE_REASON)

    override fun abandonSession() {
        scope.launch {
            try {
                closeSession(ABANDON_REASON)
            } catch (e: PickoServerException) {
                // 닫지 못하면 토큰이 남아 다음 링크 전송 때 정리된다.
                Timber.w(e, "남은 세션을 닫지 못했다")
            }
        }
    }

    private suspend fun closeSession(reason: String) {
        val token = tokenStorage.get() ?: return

        socket.close()

        try {
            dataSource.cancelSession(CancelSessionRequestDto(token = token, reason = reason))
        } catch (e: PickoServerException) {
            // 연결이 안 되거나 서버 오류면 나중에 다시 닫을 수 있게 토큰을 남긴다.
            Timber.w(e, "세션 닫기 요청 실패")
            if (e.code == null || e.code >= HTTP_SERVER_ERROR_MIN) throw e
        }

        // 닫는 동안 새 세션이 저장됐다면 그 토큰을 지우지 않는다.
        if (tokenStorage.get() == token) {
            tokenStorage.clear()
            sessionPickup = null
        }
    }

    private fun PlaceModel.toRequest(contact: ContactModel) = CreateSessionRequestDto(
        pickupLat = coordinate.latitude,
        pickupLng = coordinate.longitude,
        pickupName = name,
        vehicleDistanceM = distanceKm * METERS_PER_KM,
        vehicleEtaMin = durationMinutes.toDouble(),
        vehicleModel = TEMPORARY_VEHICLE_MODEL,
        vehiclePlate = TEMPORARY_VEHICLE_PLATE,
        passengerPhone = contact.phoneNumber.filter(Char::isDigit).ifEmpty { null },
    )
}
