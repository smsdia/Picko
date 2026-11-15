package com.offcourse.picko.core.crypto

/** 암호문에 묶는 추가 인증 데이터. 다른 용도의 암호문을 바꿔 넣으면 복호화가 실패한다. */
enum class AadContext(val raw: String) {
    SessionToken("session_token"),
}
