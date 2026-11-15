package com.offcourse.picko.data.pickuplink.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.pickuplink.repository.PickupLinkRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration.Companion.milliseconds

/** 서버와 연동하지 못하는 부분(탑승자 위치, 픽업 지점 없는 링크 전송)에 쓰는 가짜 구현. 항상 성공하고 몇 초 뒤 위치가 도착한다. */
class FakePickupLinkRepository @Inject constructor() : PickupLinkRepository {
    override suspend fun sendLink(contact: ContactModel, pickup: PlaceModel?) {
        delay(SEND_DELAY_MILLIS.milliseconds)
    }

    override suspend fun awaitPassengerLocation(): Coordinate {
        delay(LOCATION_SHARE_DELAY_MILLIS.milliseconds)
        // 서울역 근처. 주변 장소 추천 경로의 가짜 추천 지점과 겹치지 않게 동쪽으로 조금 띄운다.
        return Coordinate(latitude = 37.5562, longitude = 126.9722)
    }

    override fun observePassengerLocation(): Flow<Coordinate> = emptyFlow()

    override fun sendVehicleLocation(location: Coordinate) = Unit

    override suspend fun cancelSession() {
        delay(SEND_DELAY_MILLIS.milliseconds)
    }

    override suspend fun completeSession() = Unit

    private companion object {
        const val SEND_DELAY_MILLIS = 300L
        const val LOCATION_SHARE_DELAY_MILLIS = 3_000L
    }
}
