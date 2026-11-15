package com.offcourse.picko.core.network.di

import javax.inject.Qualifier

/** Picko 서버(`<서버 주소>`)로 가는 Retrofit */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PickoServer

/** 카카오 로컬 API(`https://dapi.kakao.com`)로 가는 Retrofit */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KakaoLocal

/** 카카오모빌리티 길찾기 API(`https://apis-navi.kakaomobility.com`)로 가는 Retrofit */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KakaoMobility

/** Picko 서버 WebSocket용 OkHttpClient. 주소에 토큰이 있어 로그를 남기지 않는다. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PickoSocket
