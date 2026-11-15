package com.offcourse.picko.data.place.model

/** 픽업 후보를 서버에서 받지 못했다(연결 실패, 서버 오류, 읽을 수 없는 응답). */
class PickupRecommendException(message: String, cause: Throwable? = null) : Exception(message, cause)
