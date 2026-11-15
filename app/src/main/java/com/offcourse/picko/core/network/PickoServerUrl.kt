package com.offcourse.picko.core.network

import com.offcourse.picko.BuildConfig

/**
 * Picko 서버 주소. `local.properties`의 `base.url`을 `BuildConfig.BASE_URL`로 받는다. REST(Retrofit)와 WebSocket(`/ws/vehicle`)이 같은 호스트를 쓴다.
 * Retrofit이 끝의 `/`를 요구해서 없으면 붙인다. 값이 비어 있으면 서버에 연결할 수 없어 바로 알린다.
 */
val PickoServerUrl: String
    get() {
        val url = BuildConfig.BASE_URL.trim()
        check(url.isNotEmpty()) { "local.properties에 base.url(Picko 서버 주소)이 없다" }

        return url.trimEnd('/') + "/"
    }
