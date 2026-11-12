package com.offcourse.picko.data.pickuplink.repository

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.contact.model.ContactModel

interface PickupLinkRepository {
    /** 탑승자에게 위치 공유 링크를 보낸다. 실패하면 예외를 던진다. */
    suspend fun sendLink(contact: ContactModel)

    /** 탑승자가 위치를 공유할 때까지 기다렸다가 그 위치를 돌려준다. */
    suspend fun awaitPassengerLocation(): Coordinate

    /** 탑승자에게 픽업이 취소되었음을 알린다. */
    suspend fun notifyCancellation(contact: ContactModel)
}
