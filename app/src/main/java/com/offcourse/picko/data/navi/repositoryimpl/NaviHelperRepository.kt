package com.offcourse.picko.data.navi.repositoryimpl

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.RouteOption
import ai.pleos.playground.navi.data.NavigationScreenRequest
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.TripRequest
import com.offcourse.picko.data.navi.datasource.NaviEvent
import com.offcourse.picko.data.navi.datasource.NaviHelperConnection
import com.offcourse.picko.data.navi.model.NaviDestinationModel
import com.offcourse.picko.data.navi.model.NaviGuidanceResult
import com.offcourse.picko.data.navi.model.NaviTripModel
import com.offcourse.picko.data.navi.repository.NaviRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

// 내비가 소요 시간을 계산해 알려 주기까지 기다리는 상한. 에뮬레이터에서 약 0.3초가 걸렸다.
private const val TRIP_TIMEOUT_MILLIS = 5_000L

// requestRoute 뒤 경로 탐색 오류가 오기를 기다리는 시간. 이 안에 오류가 없으면 탐색이 진행 중인 것으로 본다. 에뮬레이터에서 정한 값이 아니라 추정이다.
private const val GUIDANCE_ERROR_WAIT_MILLIS = 3_000L

private const val SECONDS_PER_MINUTE = 60

/** Pleos NaviHelper SDK 로 차량 내비에 묻고 맡긴다. */
class NaviHelperRepository @Inject constructor(
    private val connection: NaviHelperConnection,
) : NaviRepository {
    override suspend fun previewTrip(destination: NaviDestinationModel): NaviTripModel? {
        val helper = connection.awaitHelper() ?: return null

        return withTimeoutOrNull(TRIP_TIMEOUT_MILLIS.milliseconds) {
            coroutineScope {
                // 요청을 보내기 전에 구독을 시작해서 빠른 응답을 놓치지 않는다.
                val reply = async(start = CoroutineStart.UNDISPATCHED) {
                    connection.events.first { it is NaviEvent.Trip || it is NaviEvent.Error }
                }
                helper.requestTrip(destination.toTripRequest())

                (reply.await() as? NaviEvent.Trip)?.info?.let { trip ->
                    NaviTripModel(
                        durationMinutes = trip.eta.toMinutes(),
                        distanceMeters = trip.distance,
                    )
                }
            }
        }
    }

    override suspend fun startGuidance(destination: NaviDestinationModel): NaviGuidanceResult {
        val helper = connection.awaitHelper() ?: return NaviGuidanceResult.NotConnected

        // requestRoute 가 성공해도 탐색이 끝난 것이 아니고 실패는 onError 로 뒤늦게 온다. 보내기 전에 구독해 빠른 오류를 놓치지 않는다.
        return coroutineScope {
            val error = async(start = CoroutineStart.UNDISPATCHED) {
                withTimeoutOrNull(GUIDANCE_ERROR_WAIT_MILLIS.milliseconds) {
                    connection.events.first { it is NaviEvent.Error } as NaviEvent.Error
                }
            }
            helper.requestRoute(destination.toRouteInfo())

            error.await()?.code?.toGuidanceResult() ?: NaviGuidanceResult.Requested
        }
    }

    override suspend fun showNavigationScreen(): NaviGuidanceResult {
        val helper = connection.awaitHelper() ?: return NaviGuidanceResult.NotConnected

        helper.requestNavigationScreen(NavigationScreenRequest.Foreground)
        return NaviGuidanceResult.Requested
    }
}

fun NaviErrorCode.toGuidanceResult(): NaviGuidanceResult = when (this) {
    NaviErrorCode.CAUSE_LOW_BATTERY -> NaviGuidanceResult.LowBattery
    NaviErrorCode.CAUSE_SEARCH_ROUTE_OVER_DISTANCE -> NaviGuidanceResult.RouteTooFar
    NaviErrorCode.CAUSE_SEARCH_ROUTE_TOO_SHORT_DISTANCE -> NaviGuidanceResult.RouteTooClose
    else -> NaviGuidanceResult.RouteFailed
}

private fun Int.toMinutes(): Int = (this + SECONDS_PER_MINUTE / 2) / SECONDS_PER_MINUTE

private fun NaviDestinationModel.toTripRequest() = TripRequest(
    latitude = coordinate.latitude,
    longitude = coordinate.longitude,
    poiName = name,
    poiId = "",
    poiSubId = "",
)

private fun NaviDestinationModel.toRouteInfo() = RouteInfo(
    latitude = coordinate.latitude,
    longitude = coordinate.longitude,
    displayLatitude = null,
    displayLongitude = null,
    poiName = name,
    poiId = "",
    poiSubId = "",
    address = "",
    routeOption = RouteOption.RECOMMENDED,
)
