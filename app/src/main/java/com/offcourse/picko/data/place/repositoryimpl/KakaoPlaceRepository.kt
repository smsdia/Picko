package com.offcourse.picko.data.place.repositoryimpl

import com.offcourse.picko.BuildConfig
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.common.model.distanceMetersTo
import com.offcourse.picko.core.network.quota.KakaoApi
import com.offcourse.picko.core.network.quota.KakaoQuotaGuard
import com.offcourse.picko.data.place.datasource.KakaoLocalApi
import com.offcourse.picko.data.place.dto.KakaoKeywordSearchDto
import com.offcourse.picko.data.place.model.KakaoQuotaExceededException
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.place.repository.PlaceRepository
import com.offcourse.picko.data.route.datasource.KakaoDirectionsDataSource
import com.offcourse.picko.data.route.dto.TripSummary
import com.offcourse.picko.data.route.dto.toTripSummary
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import timber.log.Timber

private const val MAX_CANDIDATES = 3
private const val METERS_PER_KM = 1_000.0
private const val SECONDS_PER_MINUTE = 60.0

// 길찾기가 실패한 후보의 소요 시간을 직선거리로 어림할 때의 평균 속도(km/h). 측정값이 아닌 추정이다.
private const val ASSUMED_SPEED_KMH = 30.0
private const val MINUTES_PER_HOUR = 60.0

/**
 * 장소 검색은 카카오 로컬 키워드 검색으로 후보를 찾고, 후보마다 카카오모빌리티 길찾기로 거리, 소요 시간, 통행료를 채운다.
 * 주변 장소 추천은 서버 모델(추천 먼저, 세션 생성 나중)과 맞지 않아 아직 [FakePlaceRepository]의 값을 쓴다.
 * 카카오 키가 없으면 검색도 가짜 값으로 대신한다.
 */
class KakaoPlaceRepository @Inject constructor(
    private val localApi: KakaoLocalApi,
    private val directions: KakaoDirectionsDataSource,
    private val quotaGuard: KakaoQuotaGuard,
    private val fallback: FakePlaceRepository,
) : PlaceRepository {
    override suspend fun searchPlaces(query: String, vehicle: Coordinate): List<PlaceModel> {
        if (BuildConfig.KAKAO_PLATFORM.isBlank()) {
            Timber.w("카카오 키(kakao.platform.key)가 없어 가짜 장소 검색 결과를 쓴다")
            return fallback.searchPlaces(query, vehicle)
        }

        if (!quotaGuard.tryConsume(KakaoApi.LocalKeywordSearch)) throw KakaoQuotaExceededException()

        val documents = try {
            localApi.searchKeyword(
                query = query,
                x = vehicle.longitude,
                y = vehicle.latitude,
                size = MAX_CANDIDATES,
            ).documents
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 검색어와 위치는 개인정보라 메시지에 남기지 않는다. 화면에는 찾지 못한 것으로 보인다.
            Timber.w(e, "카카오 장소 검색 실패")
            logKakaoError(e)
            return emptyList()
        }

        return coroutineScope {
            documents
                .mapNotNull { document -> document.toCoordinate()?.let { document to it } }
                .map { (document, coordinate) ->
                    async { toPlace(document, coordinate, vehicle) }
                }
                .awaitAll()
        }
    }

    override suspend fun recommendNearby(passenger: Coordinate): List<PlaceModel> =
        fallback.recommendNearby(passenger)

    private suspend fun toPlace(
        document: KakaoKeywordSearchDto.Document,
        coordinate: Coordinate,
        vehicle: Coordinate,
    ): PlaceModel {
        val trip = directions.findCarRoute(origin = vehicle, destination = coordinate)?.toTripSummary()
            ?: estimateTrip(vehicle, coordinate)

        return PlaceModel(
            id = document.id,
            name = document.place_name,
            coordinate = coordinate,
            distanceKm = (trip.distanceMeters / METERS_PER_KM).roundToInt(),
            durationMinutes = (trip.durationSeconds / SECONDS_PER_MINUTE).roundToInt(),
            tollFeeWon = trip.tollWon.takeIf { it > 0 },
        )
    }

    private fun estimateTrip(vehicle: Coordinate, destination: Coordinate): TripSummary {
        val meters = vehicle.distanceMetersTo(destination)
        val minutes = meters / METERS_PER_KM / ASSUMED_SPEED_KMH * MINUTES_PER_HOUR

        return TripSummary(
            distanceMeters = meters.roundToInt(),
            durationSeconds = (minutes * SECONDS_PER_MINUTE).roundToInt(),
            tollWon = 0,
        )
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
