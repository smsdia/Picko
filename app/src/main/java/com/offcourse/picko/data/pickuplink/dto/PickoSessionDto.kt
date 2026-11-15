package com.offcourse.picko.data.pickuplink.dto

import kotlinx.serialization.Serializable

// Picko 서버 `POST /api/sessions`, `POST /api/sessions/cancel`의 요청·응답 중 쓰는 부분이다.
// null 값은 보내지 않는다(선택 필드).
@Serializable
data class CreateSessionRequestDto(
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupName: String? = null,
    val vehicleDistanceM: Double? = null,
    val vehicleEtaMin: Double? = null,
    val vehicleModel: String,
    val vehiclePlate: String,
    val passengerPhone: String? = null,
)

@Serializable
data class SessionResponseDto(
    val token: String,
    val status: String = "",
    val smsSent: Boolean = false,
    val smsDetail: String? = null,
)

@Serializable
data class CancelSessionRequestDto(
    val token: String,
    val reason: String? = null,
)
