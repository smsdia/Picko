package com.offcourse.picko.data.gleo.dto

import com.offcourse.picko.data.gleo.model.GleoActionResponseModel
import com.offcourse.picko.data.gleo.model.GleoActionStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// schema.json 의 function.name
private const val FUNCTION_REQUEST_PICKUP = "REQUEST_PICKUP"

// 앱 화면에 결과를 이미 보여주므로 Gleo 는 message 를 읽어 주기만 한다.
private const val MODE_VOICE = "voice"

// Gleo 의 APP_ACTION_RESPONSE 콜백 JSON. 필드는 공식 문서(action_callback)를 따른다.
@Serializable
data class GleoActionResponseDto(
    val functionName: String,
    val status: Status,
    val message: String,
    val mode: String,
) {
    @Serializable
    enum class Status {
        @SerialName("success")
        SUCCESS,

        @SerialName("asking")
        ASKING,

        @SerialName("fail")
        FAIL,
    }
}

fun GleoActionResponseModel.toDto(): GleoActionResponseDto = GleoActionResponseDto(
    functionName = FUNCTION_REQUEST_PICKUP,
    status = status.toDto(),
    message = message,
    mode = MODE_VOICE,
)

private fun GleoActionStatus.toDto(): GleoActionResponseDto.Status = when (this) {
    GleoActionStatus.SUCCESS -> GleoActionResponseDto.Status.SUCCESS
    GleoActionStatus.ASKING -> GleoActionResponseDto.Status.ASKING
    GleoActionStatus.FAIL -> GleoActionResponseDto.Status.FAIL
}
