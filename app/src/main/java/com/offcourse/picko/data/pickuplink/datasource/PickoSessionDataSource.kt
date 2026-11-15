package com.offcourse.picko.data.pickuplink.datasource

import com.offcourse.picko.data.pickuplink.dto.CancelSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.CreateSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.SessionResponseDto

/** Picko 서버의 세션 API. 실패하면 [com.offcourse.picko.data.pickuplink.model.PickoServerException]. */
interface PickoSessionDataSource {
    suspend fun createSession(request: CreateSessionRequestDto): SessionResponseDto

    suspend fun cancelSession(request: CancelSessionRequestDto)
}
