package com.offcourse.picko.core.localstorage.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDataStore

/** 암호화한 세션 토큰만 담는 저장소. 백업과 기기 이전에서 제외한다. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionDataStore
