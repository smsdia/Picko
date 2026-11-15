package com.offcourse.picko.data.place.model

/** 카카오 API 하루 한도에 닿아 호출하지 못했다. 흐름은 안내 뒤에 끝난다. */
class KakaoQuotaExceededException : Exception("카카오 API 하루 호출 한도")
