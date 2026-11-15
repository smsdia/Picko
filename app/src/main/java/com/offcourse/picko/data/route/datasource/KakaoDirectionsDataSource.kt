package com.offcourse.picko.data.route.datasource

import com.offcourse.picko.BuildConfig
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.network.quota.KakaoApi
import com.offcourse.picko.core.network.quota.KakaoQuotaGuard
import com.offcourse.picko.data.route.dto.KakaoDirectionsDto
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import timber.log.Timber

/** 카카오모빌리티 자동차 길찾기 호출. 실패하면 예외 대신 null을 돌려준다. */
class KakaoDirectionsDataSource @Inject constructor(
    private val api: KakaoDirectionsApi,
    private val quotaGuard: KakaoQuotaGuard,
) {
    suspend fun findCarRoute(origin: Coordinate, destination: Coordinate): KakaoDirectionsDto? {
        if (BuildConfig.KAKAO_PLATFORM.isBlank()) {
            Timber.w("카카오모빌리티 길찾기 키(kakao.platform.key)가 없다")
            return null
        }

        // 하루 한도에 닿으면 부르지 않는다. 호출하는 쪽은 null을 직선거리 추정이나 가짜 경로로 대신한다.
        if (!quotaGuard.tryConsume(KakaoApi.CarDirections)) return null

        return try {
            api.directions(
                origin = "${origin.longitude},${origin.latitude}",
                destination = "${destination.longitude},${destination.latitude}",
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 좌표는 위치 정보라 메시지에 남기지 않는다.
            Timber.w(e, "카카오모빌리티 길찾기 호출 오류")
            null
        }
    }
}
