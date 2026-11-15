package com.offcourse.picko.core.network

import com.offcourse.picko.BuildConfig

/** Picko 서버 주소(`local.properties`의 `base.url`). REST(Retrofit)와 WebSocket(`/ws/vehicle`)이 같은 호스트를 쓴다. */
val PICKO_SERVER_URL: String
    get() = BuildConfig.BASE_URL.trimEnd('/') + "/"
