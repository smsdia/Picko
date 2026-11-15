package com.offcourse.picko.data.place.repositoryimpl

import com.offcourse.picko.BuildConfig
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.network.quota.KakaoApi
import com.offcourse.picko.core.network.quota.KakaoQuotaGuard
import com.offcourse.picko.data.place.datasource.KakaoLocalApi
import com.offcourse.picko.data.place.dto.KakaoKeywordSearchDto
import com.offcourse.picko.data.place.model.KakaoQuotaExceededException
import com.offcourse.picko.data.place.repository.PlaceRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import timber.log.Timber

/**
 * 목적지는 카카오 로컬 키워드 검색의 1순위 결과를 쓴다. 후보별 길찾기는 하지 않는다. 픽업 후보는 서버가 이 좌표로 추천한다.
 * 카카오 키가 없으면 [FakePlaceRepository]의 값으로 대신한다.
 */
class KakaoPlaceRepository @Inject constructor(
    private val localApi: KakaoLocalApi,
    private val quotaGuard: KakaoQuotaGuard,
    private val fallback: FakePlaceRepository,
) : PlaceRepository {
    override suspend fun findDestination(query: String, vehicle: Coordinate): Coordinate? {
        if (BuildConfig.KAKAO_PLATFORM.isBlank()) {
            Timber.w("카카오 키(kakao.platform.key)가 없어 가짜 목적지를 쓴다")
            return fallback.findDestination(query, vehicle)
        }

        if (!quotaGuard.tryConsume(KakaoApi.LocalKeywordSearch)) throw KakaoQuotaExceededException()

        val documents = try {
            localApi.searchKeyword(
                query = query,
                x = vehicle.longitude,
                y = vehicle.latitude,
                size = 1,
            ).documents
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 검색어와 위치는 개인정보라 메시지에 남기지 않는다. 화면에는 찾지 못한 것으로 보인다.
            Timber.w(e, "카카오 장소 검색 실패")
            logKakaoError(e)
            return null
        }

        return documents.firstNotNullOfOrNull { it.toCoordinate() }
    }
}

private fun KakaoKeywordSearchDto.Document.toCoordinate(): Coordinate? {
    val longitude = x.toDoubleOrNull() ?: return null
    val latitude = y.toDoubleOrNull() ?: return null

    return Coordinate(latitude = latitude, longitude = longitude)
}

// 카카오 오류 본문(errorType, message)은 사용 설정이 꺼진 403처럼 원인을 가려야 할 때만 디버그 빌드에서 남긴다.
// 키가 잘못된 401은 본문에 `appKey(<키>)`가 들어 있어 키 부분은 가려서 남긴다.
private val AppKeyPattern = Regex("""appKey\([^)]*\)""")

private fun logKakaoError(e: Exception) {
    if (!BuildConfig.DEBUG || e !is HttpException) return

    val body = try {
        e.response()?.errorBody()?.string()
    } catch (ignored: Exception) {
        null
    }
    Timber.w("카카오 오류 응답: HTTP %d %s", e.code(), body?.replace(AppKeyPattern, "appKey(***)"))
}
