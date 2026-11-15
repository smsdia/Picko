package com.offcourse.picko.debug.navi

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.constants.RouteOption
import ai.pleos.playground.navi.data.CurrentLocationInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.DrivingInfo
import ai.pleos.playground.navi.data.NavigationScreenRequest
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.RouteOverviewShowInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteStateInfo
import ai.pleos.playground.navi.data.TBTInfo
import ai.pleos.playground.navi.data.TripInfo
import ai.pleos.playground.navi.data.TripRequest
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import timber.log.Timber

/** NaviHelper 호출과 콜백을 화면의 로그 목록으로 보여 주는 실험용 컨트롤러. */
class NaviExperimentController(private val context: Context) {
    private val naviHelper = NaviHelper(context)
    private val mainHandler = Handler(Looper.getMainLooper())

    var connectionState by mutableStateOf("연결 전")
        private set
    val logs = mutableStateListOf<String>()

    // 콜백이 어느 스레드로 오는지 문서에 없어서, 상태 변경은 항상 메인 스레드로 넘긴다.
    private fun log(message: String) {
        Timber.d("NaviExperiment: %s", message)
        mainHandler.post { logs.add(0, message) }
    }

    private val listener = object : NaviHelperEventListener {
        override fun onRouteStarted(routeStartInfo: RouteStartInfo) =
            log("onRouteStarted $routeStartInfo")

        override fun onRoutingCompleted() = log("onRoutingCompleted")
        override fun onRouteEnded() = log("onRouteEnded")
        override fun onRouteCancelled() = log("onRouteCancelled")
        override fun onReRouteEnded() = log("onReRouteEnded")
        override fun onDestinationChanged() = log("onDestinationChanged")
        override fun onNavigationStatus(status: NavigationStatus) = log("onNavigationStatus $status")
        override fun onTripInfo(tripInfo: TripInfo) = log("onTripInfo $tripInfo")
        override fun onRouteStateInfo(info: RouteStateInfo) = log("onRouteStateInfo $info")
        override fun onDestinationInfo(info: DestinationInfo) = log("onDestinationInfo $info")
        override fun onTBTInfo(list: List<TBTInfo>) = log("onTBTInfo ${list.size}건 $list")
        override fun onDrivingInfo(info: DrivingInfo) = log("onDrivingInfo $info")
        override fun onCurrentLocationInfo(info: CurrentLocationInfo) =
            log("onCurrentLocationInfo $info")

        override fun onError(errorCode: NaviErrorCode) = log("onError $errorCode")
    }

    fun connect() {
        naviHelper.initialize(
            onConnected = {
                naviHelper.addListener(listener)
                mainHandler.post { connectionState = "연결됨" }
                log("initialize 성공")
            },
            onFailed = { throwable ->
                mainHandler.post { connectionState = "연결 실패" }
                log("initialize 실패 $throwable")
            },
        )
    }

    fun release() {
        naviHelper.removeListener(listener)
        naviHelper.release()
    }

    // A-1: 안내를 시작하지 않고 거리와 도착 예정만 받는다.
    fun previewPickupTrip() {
        log("requestTrip ${PICKUP.name}")
        naviHelper.requestTrip(
            TripRequest(PICKUP.latitude, PICKUP.longitude, PICKUP.name, PICKUP.poiId, PICKUP.poiSubId),
        )
    }

    // A-2: 차량 내비에 목적지를 주고 안내를 시작시킨다.
    fun requestPickupRoute() = requestRoute(PICKUP)

    // C: 픽업 뒤 목적지 안내. 호출은 A-2와 같고 목적지만 다르다.
    fun requestDestinationRoute() = requestRoute(DESTINATION)

    // B-1: 내비 화면을 전면으로 올린다.
    fun showNavigationForeground() {
        log("requestNavigationScreen Foreground")
        naviHelper.requestNavigationScreen(NavigationScreenRequest.Foreground)
    }

    // B-2: 경로 전체 보기 화면을 띄운다.
    fun showRouteOverview() {
        log("requestNavigationScreen OverviewRoute ${OVERVIEW_DURATION_MS}ms")
        naviHelper.requestNavigationScreen(
            NavigationScreenRequest.OverviewRoute(RouteOverviewShowInfo(OVERVIEW_DURATION_MS)),
        )
    }

    // 분할 시험: 내비 앱을 LAUNCH_ADJACENT 로 띄워 옆 창에 놓이는지 본다. 내비 앱 이름은 에뮬레이터에서 확인한 값이다.
    fun launchNaviAdjacent() {
        log("startActivity 내비 앱 LAUNCH_ADJACENT")
        val intent = Intent().apply {
            component = ComponentName(NAVI_APP_PACKAGE, "$NAVI_APP_PACKAGE.MainActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { log("startActivity 실패 $it") }
    }

    fun cancelRoute() {
        log("cancelRoute")
        naviHelper.cancelRoute()
    }

    fun queryStates() {
        log("getRouteStateInfo, getDestinationInfo, getTBTInfo")
        naviHelper.getRouteStateInfo()
        naviHelper.getDestinationInfo()
        naviHelper.getTBTInfo()
        naviHelper.getCurrentLocationInfo()
    }

    private fun requestRoute(place: TestPlace) {
        log("requestRoute ${place.name}")
        naviHelper.requestRoute(
            RouteInfo(
                latitude = place.latitude,
                longitude = place.longitude,
                displayLatitude = null,
                displayLongitude = null,
                poiName = place.name,
                poiId = place.poiId,
                poiSubId = place.poiSubId,
                address = place.address,
                routeOption = RouteOption.RECOMMENDED,
            ),
        )
    }

    private class TestPlace(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val address: String,
        val poiId: String = "",
        val poiSubId: String = "",
    )

    private companion object {
        const val NAVI_APP_PACKAGE = "ai.umos.maps.android.navigation.app"
        const val OVERVIEW_DURATION_MS = 5000
        val PICKUP = TestPlace("픽업 테스트 지점", 37.4946, 127.0576, "서울 강남구 대치동")
        val DESTINATION = TestPlace("선릉역", 37.5045, 127.0490, "서울 강남구 테헤란로")
    }
}
