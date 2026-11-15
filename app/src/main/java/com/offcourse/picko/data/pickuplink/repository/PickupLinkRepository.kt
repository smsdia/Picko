package com.offcourse.picko.data.pickuplink.repository

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.place.model.PlaceModel
import kotlinx.coroutines.flow.Flow

interface PickupLinkRepository {
    /**
     * 픽업 지점 [pickup]으로 세션을 만들고 탑승자에게 위치 공유 링크를 보낸다. 실패하면 예외를 던진다.
     */
    suspend fun sendLink(contact: ContactModel, pickup: PlaceModel)

    /**
     * 탑승자가 위치를 공유할 때까지 기다렸다가 그 위치를 돌려준다.
     * 기다리는 중에 서버가 세션을 끝내면 [com.offcourse.picko.data.pickuplink.model.PickupSessionEndedException]을 던진다.
     */
    suspend fun awaitPassengerLocation(): Coordinate

    /** 탑승자가 위치를 보낼 때마다(1초 간격) 그 위치를 내보낸다. 같은 위치는 건너뛴다. 세션이 없으면 아무것도 내보내지 않는다. */
    fun observePassengerLocation(): Flow<Coordinate>

    /** 차량의 현재 위치를 탑승자에게 보낸다. 연결되어 있지 않으면 보내지 않는다. */
    fun sendVehicleLocation(location: Coordinate)

    /** 진행 중인 세션을 서버에서 취소하고 저장된 토큰을 지운다. 세션이 없으면 아무것도 하지 않는다. */
    suspend fun cancelSession()

    /**
     * 픽업이 정상으로 끝났다. 서버를 닫고 저장된 토큰을 지운다. 서버에는 완료 API가 없어 취소 API(사유 `pickup_completed`)로 세션을 닫는다.
     * 그래서 탑승자 웹에는 "운전자 요청으로 픽업이 취소되었어요"가 뜬다. 세션이 없으면 아무것도 하지 않는다.
     */
    suspend fun completeSession()

    /**
     * 흐름이 끝났는데 세션이 닫히지 않은 채 남지 않게, 열려 있는 세션이 있으면 백그라운드에서 서버에 닫는다.
     * 호출한 쪽(ViewModel)이 사라져도 이어서 실행된다. 세션이 없거나 이미 닫혔으면 아무것도 하지 않고, 실패해도 예외를 던지지 않는다.
     */
    fun abandonSession()
}
