package com.offcourse.picko.core.network.auth

import okhttp3.Interceptor
import okhttp3.Response

/** 카카오 REST API 키를 `Authorization: KakaoAK` 헤더로 붙인다. 키는 호출하는 쪽이 [apiKey]로 넘긴다. */
class KakaoAuthInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("Authorization", "KakaoAK $apiKey")
            .build()

        return chain.proceed(request)
    }
}
