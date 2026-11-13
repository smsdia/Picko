package com.offcourse.picko.data.route.datasource

import com.offcourse.picko.BuildConfig
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.route.dto.KakaoDirectionsDto
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber

private const val DIRECTIONS_URL = "https://apis-navi.kakaomobility.com/v1/directions"
private const val TIMEOUT_MILLIS = 5_000

/** 카카오모빌리티 자동차 길찾기 호출. 실패하면 예외 대신 null을 돌려준다. */
@Singleton
class KakaoDirectionsDataSource @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun findCarRoute(origin: Coordinate, destination: Coordinate): KakaoDirectionsDto? =
        withContext(Dispatchers.IO) {
            val key = BuildConfig.KAKAO_PLATFORM
            if (key.isBlank()) {
                Timber.w("카카오모빌리티 길찾기 키(kakao.platform.key)가 없다")
                return@withContext null
            }

            // 좌표는 경도,위도 순서다.
            val query = "origin=${origin.longitude},${origin.latitude}" +
                "&destination=${destination.longitude},${destination.latitude}"
            val connection = URL("$DIRECTIONS_URL?$query").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT_MILLIS
                connection.readTimeout = TIMEOUT_MILLIS
                connection.setRequestProperty("Authorization", "KakaoAK $key")
                connection.setRequestProperty("Content-Type", "application/json")

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    Timber.w("카카오모빌리티 길찾기 실패: HTTP %d", connection.responseCode)
                    return@withContext null
                }
                json.decodeFromString<KakaoDirectionsDto>(connection.inputStream.bufferedReader().readText())
            } catch (e: Exception) {
                Timber.w(e, "카카오모빌리티 길찾기 호출 오류")
                null
            } finally {
                connection.disconnect()
            }
        }
}
