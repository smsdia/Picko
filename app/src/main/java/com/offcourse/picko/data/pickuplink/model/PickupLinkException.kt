package com.offcourse.picko.data.pickuplink.model

/** 서버 호출이 실패했다. [code]는 HTTP 상태 코드이고 네트워크 오류면 null. */
class PickoServerException(val code: Int?, message: String, cause: Throwable? = null) : Exception(message, cause)

/** 세션은 만들어졌지만 탑승자에게 문자가 가지 않았다. */
class SmsNotSentException(detail: String?) : Exception("탑승자 문자 발송 실패: $detail")

/** 탑승자 위치를 기다리는 동안 서버가 세션을 끝냈다(취소됐거나 토큰이 유효하지 않다). */
class PickupSessionEndedException(val isCanceled: Boolean) : Exception("픽업 세션이 끝났다")
