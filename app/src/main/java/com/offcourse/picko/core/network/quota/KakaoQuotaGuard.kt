package com.offcourse.picko.core.network.quota

/** 카카오 API를 하루 무료 쿼터 안에서만 쓰도록 이 기기의 호출 수를 센다. */
interface KakaoQuotaGuard {
    /** 오늘 [api]를 [count]번 더 불러도 한도를 넘지 않으면 true. 세지는 않는다. */
    suspend fun hasRoom(api: KakaoApi, count: Int = 1): Boolean

    /** 호출 전에 부른다. 한도를 넘지 않으면 [count]만큼 세고 true, 넘으면 세지 않고 false. */
    suspend fun tryConsume(api: KakaoApi, count: Int = 1): Boolean
}
