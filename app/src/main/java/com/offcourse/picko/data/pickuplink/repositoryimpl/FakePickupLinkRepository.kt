package com.offcourse.picko.data.pickuplink.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.pickuplink.repository.PickupLinkRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** 링크 전달 방식과 탑승자 웹 페이지가 정해지기 전에 쓰는 가짜 구현. 항상 성공하고 몇 초 뒤 위치가 도착한다. */
class FakePickupLinkRepository @Inject constructor() : PickupLinkRepository {
    override suspend fun sendLink(contact: ContactModel) {
        delay(SEND_DELAY_MILLIS.milliseconds)
    }

    override suspend fun awaitPassengerLocation(): Coordinate {
        delay(LOCATION_SHARE_DELAY_MILLIS.milliseconds)
        // 지도에서 픽업 지점(대치동 학원가 사거리) 마커와 배지가 겹치지 않도록 동쪽으로 1.6km쯤 띄운다.
        return Coordinate(latitude = 37.4952, longitude = 127.0750)
    }

    override suspend fun notifyCancellation(contact: ContactModel) {
        delay(SEND_DELAY_MILLIS.milliseconds)
    }

    private companion object {
        const val SEND_DELAY_MILLIS = 300L
        const val LOCATION_SHARE_DELAY_MILLIS = 3_000L
    }
}
