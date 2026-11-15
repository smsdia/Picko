package com.offcourse.picko.data.vehicle.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.common.model.distanceMetersTo
import com.offcourse.picko.data.route.datasource.KakaoDirectionsDataSource
import com.offcourse.picko.data.route.dto.toRouteModel
import com.offcourse.picko.data.vehicle.model.VehicleLocation
import com.offcourse.picko.data.vehicle.repository.VehicleRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// 시연 차량의 속도(m/s)와 위치 갱신 간격. 40m/s 는 시속 144km 로 시연이 길어지지 않게 빠르게 잡았다.
const val DEMO_SPEED_METERS_PER_SECOND = 40.0
private const val DEMO_TICK_MILLIS = 1_000L

// 시연 차량이 서 있는 출발지(서울역). 앱을 시작하면 여기서 출발한다.
private val DemoOrigin = Coordinate(37.5547, 126.9707)

/**
 * 시연용 가짜 차량. 내비게이션 안내 시작을 누르면 지금 위치에서 선택한 픽업 지점까지 카카오모빌리티 자동차 경로의 좌표를
 * 일정한 속도로 따라가며 위치를 1초마다 내보내고, 도착하면 멈춘다. 길찾기를 쓰지 못하면 두 지점을 잇는 직선을 따라간다.
 * 안내를 다시 시작하면 지금 위치에서 새로 출발하고, 앱을 다시 시작하면 처음 출발지로 돌아간다.
 * 빌드 종류와 상관없이 `demo.drive`가 켜졌을 때만 쓴다. 제출용 시연 APK는 켠 채로 만들고, 실차 테스트는 `-Pdemo.drive=false`로 다시 빌드한다.
 */
@Singleton
class DemoVehicleRepository @Inject constructor(
    private val directions: KakaoDirectionsDataSource,
) : VehicleRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _position = MutableStateFlow(DemoOrigin)
    private var driveJob: Job? = null

    /** 시연 차량의 지금 위치. 시연용 내비가 남은 거리를 셀 때 쓴다. */
    val position: StateFlow<Coordinate> = _position

    /** 안내 시작을 눌러 출발한 뒤인지. 시연용 내비는 출발한 뒤에만 시간을 직접 계산한다. */
    @Volatile
    var isDriving: Boolean = false
        private set

    override fun observeLocation() = _position
        .map { VehicleLocation(it.latitude, it.longitude, accuracyMeters = 5f, timestampMillis = System.currentTimeMillis()) }

    /** 안내 시작을 누르면 [destination]으로 출발한다. 그 전에는 서 있다. */
    override fun onNavigationStarted(destination: Coordinate) = start(destination)

    @Synchronized
    private fun start(destination: Coordinate) {
        driveJob?.cancel()
        isDriving = true
        val origin = _position.value

        driveJob = scope.launch {
            val route = directions.findCarRoute(origin, destination)?.toRouteModel()?.points
            val path = buildDemoPath(route ?: listOf(origin, destination), stepMeters = DEMO_SPEED_METERS_PER_SECOND * DEMO_TICK_MILLIS / 1_000)

            path.forEach { point ->
                _position.value = point
                delay(DEMO_TICK_MILLIS)
            }
        }
    }
}

/** [points]를 따라 [stepMeters]마다 한 점씩 찍은 경로. 처음과 끝 지점은 반드시 포함한다. */
fun buildDemoPath(points: List<Coordinate>, stepMeters: Double): List<Coordinate> {
    val result = mutableListOf(points.first())
    var carry = 0.0 // 마지막으로 찍은 점부터 지금 선분 시작까지 온 거리

    points.zipWithNext().forEach { (from, to) ->
        val length = from.distanceMetersTo(to)
        if (length == 0.0) return@forEach

        var next = stepMeters - carry // 지금 선분에서 다음 점까지의 거리
        while (next <= length) {
            val ratio = next / length
            result += Coordinate(
                latitude = from.latitude + (to.latitude - from.latitude) * ratio,
                longitude = from.longitude + (to.longitude - from.longitude) * ratio,
            )
            next += stepMeters
        }
        carry = length - (next - stepMeters)
    }
    if (result.last() != points.last()) result += points.last()

    return result
}
