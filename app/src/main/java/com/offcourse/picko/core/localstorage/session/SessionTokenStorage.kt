package com.offcourse.picko.core.localstorage.session

/** 진행 중인 픽업 세션의 토큰. 디스크에는 암호화해서 둔다. */
interface SessionTokenStorage {
    /** 저장된 토큰. 없거나 복호화할 수 없으면 null. */
    suspend fun get(): String?

    /** 토큰을 저장한다. 디스크 저장에 실패해도 이 프로세스가 사는 동안은 [get]이 값을 돌려준다. */
    suspend fun save(token: String)

    suspend fun clear()
}
