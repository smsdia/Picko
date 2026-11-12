package com.offcourse.picko.data.gleo.model

import androidx.compose.runtime.Immutable

enum class GleoActionStatus {
    SUCCESS,
    ASKING,
    FAIL,
}

// message 는 Gleo 가 읽는 영어 완전한 문장이다. 화면에 보이는 문자열이 아니다.
@Immutable
data class GleoActionResponseModel(
    val status: GleoActionStatus,
    val message: String,
)
