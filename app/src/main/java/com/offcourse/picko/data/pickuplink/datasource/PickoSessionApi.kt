package com.offcourse.picko.data.pickuplink.datasource

import com.offcourse.picko.data.pickuplink.dto.CancelSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.CreateSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.SessionResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Picko 서버의 세션 API(스웨거 `/v3/api-docs`). */
interface PickoSessionApi {
    @POST("api/sessions")
    suspend fun createSession(@Body request: CreateSessionRequestDto): SessionResponseDto

    @POST("api/sessions/cancel")
    suspend fun cancelSession(@Body request: CancelSessionRequestDto)
}
