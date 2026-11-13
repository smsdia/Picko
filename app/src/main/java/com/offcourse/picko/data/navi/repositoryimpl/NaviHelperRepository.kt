package com.offcourse.picko.data.navi.repositoryimpl

import ai.pleos.playground.navi.constants.RouteOption
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.TripRequest
import com.offcourse.picko.data.navi.datasource.NaviEvent
import com.offcourse.picko.data.navi.datasource.NaviHelperConnection
import com.offcourse.picko.data.navi.model.NaviDestinationModel
import com.offcourse.picko.data.navi.model.NaviTripModel
import com.offcourse.picko.data.navi.repository.NaviRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

// 내비가 소요 시간을 계산해 알려 주기까지 기다리는 상한. 에뮬레이터에서 약 0.3초가 걸렸다.
private const val TRIP_TIMEOUT_MILLIS = 5_000L

private const val SECONDS_PER_MINUTE = 60

/** Pleos NaviHelper SDK 로 차량 내비에 묻고 맡긴다. */
class NaviHelperRepository @Inject constructor(
    private val connection: NaviHelperConnection,
) : NaviRepository {
    override suspend fun previewTrip(destination: NaviDestinationModel): NaviTripModel? {
        val helper = connection.awaitHelper() ?: return null

        return withTimeoutOrNull(TRIP_TIMEOUT_MILLIS) {
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

    override suspend fun startGuidance(destination: NaviDestinationModel): Boolean {
        val helper = connection.awaitHelper() ?: return false

        helper.requestRoute(destination.toRouteInfo())
        return true
    }
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
