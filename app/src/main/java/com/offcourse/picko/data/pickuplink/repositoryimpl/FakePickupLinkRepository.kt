package com.offcourse.picko.data.pickuplink.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.pickuplink.repository.PickupLinkRepository
import javax.inject.Inject
import kotlinx.coroutines.delay

/** 링크 전달 방식과 탑승자 웹 페이지가 정해지기 전에 쓰는 가짜 구현. 항상 성공하고 몇 초 뒤 위치가 도착한다. */
class FakePickupLinkRepository @Inject constructor() : PickupLinkRepository {
    override suspend fun sendLink(contact: ContactModel) {
        delay(SEND_DELAY_MILLIS)
    }

    override suspend fun awaitPassengerLocation(): Coordinate {
        delay(LOCATION_SHARE_DELAY_MILLIS)
        return Coordinate(latitude = 37.4952, longitude = 127.0641)
    }

    override suspend fun notifyCancellation(contact: ContactModel) {
        delay(SEND_DELAY_MILLIS)
    }

    private companion object {
        const val SEND_DELAY_MILLIS = 300L
        const val LOCATION_SHARE_DELAY_MILLIS = 3_000L
    }
}
