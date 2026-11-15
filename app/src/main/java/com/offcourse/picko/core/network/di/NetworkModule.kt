package com.offcourse.picko.core.network.di

import com.offcourse.picko.BuildConfig
import com.offcourse.picko.core.network.PickoServerUrl
import com.offcourse.picko.core.network.auth.KakaoAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val KAKAO_LOCAL_BASE_URL = "https://dapi.kakao.com/"
private const val KAKAO_MOBILITY_BASE_URL = "https://apis-navi.kakaomobility.com/"
private const val CONNECT_TIMEOUT_SECONDS = 10L
private const val READ_TIMEOUT_SECONDS = 10L
private const val SOCKET_PING_INTERVAL_SECONDS = 25L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        // 선택 필드가 null이면 요청에 싣지 않는다.
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideBaseOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor())
        .build()

    // WebSocket 접속 주소의 쿼리에 세션 토큰이 있어 요청 로그를 남기는 인터셉터를 붙이지 않는다.
    // 연결이 끊긴 것을 알아채고 유휴 연결이 끊기지 않게 ping을 보낸다.
    @Provides
    @Singleton
    @PickoSocket
    fun providePickoSocketClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .pingInterval(SOCKET_PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @PickoServer
    fun providePickoRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(baseUrl = PickoServerUrl, client = client, json = json)

    @Provides
    @Singleton
    @KakaoLocal
    fun provideKakaoLocalRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(baseUrl = KAKAO_LOCAL_BASE_URL, client = kakaoClient(client), json = json)

    @Provides
    @Singleton
    @KakaoMobility
    fun provideKakaoMobilityRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(baseUrl = KAKAO_MOBILITY_BASE_URL, client = kakaoClient(client), json = json)

    private fun kakaoClient(base: OkHttpClient): OkHttpClient = base.newBuilder()
        .addInterceptor(KakaoAuthInterceptor(BuildConfig.KAKAO_PLATFORM))
        .build()

    private fun retrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // 요청과 응답 본문에는 토큰, 전화번호, 위치가 있어 남기지 않는다. 디버그 빌드에서만 메서드, 주소, 상태 코드를 남긴다.
    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("Set-Cookie")
    }
}
