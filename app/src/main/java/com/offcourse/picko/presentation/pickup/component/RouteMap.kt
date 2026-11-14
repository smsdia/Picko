package com.offcourse.picko.presentation.pickup.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.route.model.remainingPointsFrom

// 카카오 지도 줌 레벨. 클수록 넓게 보인다.
private const val SINGLE_POINT_ZOOM_LEVEL = 16

// 가까운 점들을 맞출 때 이 레벨보다 더 확대하지 않는다. 차량이 픽업 지점에 가까워져도 지도가 과하게 커지지 않게 한다.
private const val FIT_MAX_ZOOM_LEVEL = 17

// 카메라를 옮기는 애니메이션 길이(ms)와, 시작 알림 뒤 끝 알림이 오지 않을 때 기다리는 상한(ms)
private const val CAMERA_MOVE_MILLIS = 800
private const val CAMERA_MOVE_TIMEOUT_MILLIS = 1_500L

// 위치가 바뀔 때 마커가 다음 위치로 미끄러지는 시간(ms). 위치가 1초마다 오므로 한 주기에 가깝게 둔다.
private const val MARKER_MOVE_MILLIS = 900

// 차량이 움직이는 동안 카메라를 다시 맞추는 주기(ms)
private const val CAMERA_REFIT_INTERVAL_MILLIS = 10_000L

// 지나간 경로를 지우는 애니메이션 길이(ms). 차량 위치가 1초마다 바뀌어도 끊기지 않게 한 주기에 가깝게 둔다.
private const val ROUTE_PROGRESS_DURATION_MILLIS = 900

// 경로선 굵기(px)
private const val ROUTE_LINE_WIDTH = 16f

// 마커가 잘리지 않도록 지도 가장자리에 두는 여백(px)
private const val MAP_HORIZONTAL_PADDING = 160
private const val MAP_VERTICAL_PADDING = 110

// 배지와 라벨 사이 간격
private val LabelGap = 14.dp

// 지도를 움직이는 제스처. 마커를 지도 위에 겹쳐 그리므로 카메라는 코드가 정한 위치에 고정한다.
private val CameraGestures = listOf(
    GestureType.Pan,
    GestureType.Zoom,
    GestureType.Rotate,
    GestureType.Tilt,
    GestureType.RotateZoom,
    GestureType.OneFingerZoom,
    GestureType.TwoFingerSingleTap,
    GestureType.OneFingerDoubleTap,
    GestureType.LongTapAndDrag,
)

/**
 * 지도에 그릴 경로선. [points]는 순서대로 이어진다. 값으로 비교해 같은 경로면 지도를 다시 그리지 않는다.
 * [progressFrom]이 있으면 그 위치까지 지나간 구간을 지도가 부드럽게 지운다.
 */
data class MapRouteLine(
    val points: List<Coordinate>,
    val color: Color,
    val progressFrom: Coordinate? = null,
)

/**
 * 지도 위 [coordinate]에 겹쳐 그릴 마커. [badge]의 중심이 좌표에 놓이고, [label]은 지도 안에서 다른 마커와 겹치지 않는
 * 쪽(오른쪽, 왼쪽, 위, 아래 순)에 놓인다. 자리가 모자라면 [priority]가 작은 마커가 먼저 자리를 얻는다.
 */
class MapMarkerSlot(
    val coordinate: Coordinate,
    val priority: Int = 0,
    val badge: @Composable () -> Unit,
    val label: @Composable () -> Unit,
)

/**
 * 경로선과 마커를 보여 주는 카카오 지도. 카메라는 경로선과 마커가 모두 보이도록 맞춘다.
 * 카카오 SDK 타입은 이 파일 밖으로 내보내지 않는다. Preview 에서는 지도를 그리지 못해 마커만 보인다.
 */
@Composable
fun RouteMap(
    routes: List<MapRouteLine>,
    markers: List<MapMarkerSlot>,
    onMapError: (Exception) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 카카오 지도는 Preview 에서 그릴 수 없다.
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(PickoTheme.colors.surfaceIce))
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember(context) { MapView(context) }
    val currentOnMapError by rememberUpdatedState(onMapError)
    val currentMarkers by rememberUpdatedState(markers)
    var kakaoMap by remember(mapView) { mutableStateOf<KakaoMap?>(null) }
    var markerPoints by remember { mutableStateOf<List<IntOffset?>>(emptyList()) }
    // 카메라가 움직이는 중이면 마커는 미끄러지지 않고 지도에 붙어 가야 한다. 카메라가 움직인 횟수로도 그 순간을 알아본다.
    var isCameraMoving by remember { mutableStateOf(false) }
    var cameraMoveCount by remember { mutableIntStateOf(0) }
    var seenCameraMoveCount by remember { mutableIntStateOf(0) }
    val shouldSnapMarkers = isCameraMoving || seenCameraMoveCount != cameraMoveCount
    SideEffect { seenCameraMoveCount = cameraMoveCount }

    val initialCenter = routes.firstOrNull()?.points?.firstOrNull() ?: markers.firstOrNull()?.coordinate

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )

        val animatedPoints = markers.indices.map { index ->
            key(index) {
                val target = markerPoints.getOrNull(index)

                if (target == null) {
                    null
                } else {
                    animateIntOffsetAsState(
                        targetValue = target,
                        animationSpec = if (shouldSnapMarkers) snap() else tween(MARKER_MOVE_MILLIS, easing = LinearEasing),
                        label = "markerPosition",
                    ).value
                }
            }
        }

        MarkerLayer(markers = markers, points = animatedPoints, modifier = Modifier.fillMaxSize())
    }

    DisposableEffect(lifecycleOwner, mapView) {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    kakaoMap = null
                }

                override fun onMapError(error: Exception) {
                    kakaoMap = null
                    currentOnMapError(error)
                }
            },
            object : KakaoMapReadyCallback() {
                override fun getPosition(): LatLng = initialCenter?.toLatLng() ?: LatLng.from(0.0, 0.0)

                override fun getZoomLevel(): Int = SINGLE_POINT_ZOOM_LEVEL

                override fun onMapReady(map: KakaoMap) {
                    CameraGestures.forEach { gesture -> map.setGestureEnable(gesture, false) }
                    map.setPadding(MAP_HORIZONTAL_PADDING, MAP_VERTICAL_PADDING, MAP_HORIZONTAL_PADDING, MAP_VERTICAL_PADDING)
                    map.setOnCameraMoveStartListener { _, _ -> isCameraMoving = true }
                    map.setOnCameraMoveEndListener { movedMap, _, _ ->
                        markerPoints = currentMarkers.projectOn(movedMap)
                        cameraMoveCount++
                        isCameraMoving = false
                    }

                    kakaoMap = map
                }
            },
        )

        // 이미 RESUMED 상태에서 등록해도 현재 상태까지의 이벤트를 전달받는다.
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            kakaoMap = null
            mapView.finish()
        }
    }

    // 호출할 때마다 새로 만들어지는 리스트 대신 값을 키로 쓴다. 같은 좌표면 효과를 다시 돌리지 않아 경로선이 깜빡이지 않는다.
    val markerCoordinates = markers.map { it.coordinate }
    val drawableRoutes = routes.filter { it.points.size >= 2 }
    val routeShapes = drawableRoutes.map { it.points to it.color }
    val routeProgresses = drawableRoutes.map { it.progressFrom }
    val routeLines = remember(mapView) { mutableListOf<RouteLine>() }

    // 경로선 모양은 바뀔 때만 올린다. 차량이 움직일 때마다 올리지 않고 아래에서 지나간 구간만 지운다.
    LaunchedEffect(kakaoMap, routeShapes) {
        val map = kakaoMap ?: return@LaunchedEffect

        map.routeLineManager?.layer?.let { layer ->
            val segments = drawableRoutes.map { route ->
                RouteLineSegment.from(
                    route.points.map { it.toLatLng() },
                    RouteLineStyle.from(ROUTE_LINE_WIDTH, route.color.toArgb()),
                )
            }

            if (routeLines.size == segments.size) {
                // 선의 개수가 같으면 지우지 않고 좌표만 바꾼다.
                routeLines.forEachIndexed { index, line -> line.changeSegments(segments[index]) }
            } else {
                layer.removeAll()
                routeLines.clear()
                segments.forEach { segment -> routeLines += layer.addRouteLine(RouteLineOptions.from(segment)) }
            }
        }
    }

    // 지나간 구간은 progressTo 로 시작점에서부터 부드럽게 지운다. 모양을 새로 올리면 progress 가 처음으로 돌아가므로 다시 맞춘다.
    LaunchedEffect(kakaoMap, routeShapes, routeProgresses) {
        routeLines.forEachIndexed { index, line ->
            val position = routeProgresses.getOrNull(index) ?: return@forEachIndexed
            val progress = line.getProgressFromPoint(position.toLatLng())
            if (progress.isNaN()) return@forEachIndexed

            line.progressTo(progress.coerceIn(0f, 1f), ROUTE_PROGRESS_DURATION_MILLIS)
        }
    }

    // 카메라는 위치가 바뀔 때마다 옮기지 않는다. 경로 모양이 바뀔 때 한 번, 그 뒤에는 주기마다 최신 값으로 맞춘다.
    val latestRoutes by rememberUpdatedState(drawableRoutes)
    val latestMarkerCoordinates by rememberUpdatedState(markerCoordinates)

    LaunchedEffect(kakaoMap, routeShapes) {
        val map = kakaoMap ?: return@LaunchedEffect

        fitCamera(map, latestRoutes, latestMarkerCoordinates, animation = null)
    }

    // 주기마다 맞출 때는 카메라가 튀지 않게 애니메이션으로 옮긴다.
    LaunchedEffect(kakaoMap) {
        val map = kakaoMap ?: return@LaunchedEffect

        while (true) {
            delay(CAMERA_REFIT_INTERVAL_MILLIS)
            fitCamera(map, latestRoutes, latestMarkerCoordinates, CameraAnimation.from(CAMERA_MOVE_MILLIS))
        }
    }

    // 카메라가 움직이는 동안에는 끝 알림만 기다리면 마커가 뒤처지므로 프레임마다 화면 위치를 다시 구한다.
    // 끝 알림이 오지 않는 경우(움직임이 없었던 경우)를 대비해 기다리는 시간에 상한을 둔다.
    LaunchedEffect(kakaoMap, isCameraMoving) {
        val map = kakaoMap ?: return@LaunchedEffect
        if (!isCameraMoving) return@LaunchedEffect

        withTimeoutOrNull(CAMERA_MOVE_TIMEOUT_MILLIS) {
            while (true) {
                withFrameNanos { }
                markerPoints = currentMarkers.projectOn(map)
            }
        }
        markerPoints = currentMarkers.projectOn(map)
        isCameraMoving = false
    }

    // 마커는 카메라와 별개로 좌표가 바뀔 때마다 화면 위치를 다시 구한다. 그래야 카메라가 멈춰 있어도 차량 마커가 움직인다.
    LaunchedEffect(kakaoMap, markerCoordinates) {
        val map = kakaoMap ?: return@LaunchedEffect

        markerPoints = markers.projectOn(map)
    }
}

/**
 * 배지는 [points]의 화면 좌표에 중심을 맞춰 놓고, 라벨은 배지 곁의 빈 자리에 놓는다.
 * 배지를 모두 먼저 놓고 라벨을 [MapMarkerSlot.priority] 순서로 놓으므로 라벨이 어느 마커의 배지도 가리지 않는다.
 * 한 번 정한 쪽은 아직 들어맞으면 바꾸지 않아, 마커가 움직여도 라벨이 이쪽저쪽으로 튀지 않는다.
 */
@Composable
private fun MarkerLayer(
    markers: List<MapMarkerSlot>,
    points: List<IntOffset?>,
    modifier: Modifier = Modifier,
) {
    val lastSides = remember { mutableMapOf<Int, LabelSide>() }

    Layout(
        content = {
            // 그리는 순서: 배지가 아래, 라벨이 위
            markers.forEach { Box { it.badge() } }
            markers.forEach { Box { it.label() } }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val badges = measurables.take(markers.size).map { it.measure(loose) }
        val labels = measurables.drop(markers.size).map { it.measure(loose) }
        val bounds = IntRect(0, 0, constraints.maxWidth, constraints.maxHeight)
        val gap = LabelGap.roundToPx()

        val badgeRects = badges.mapIndexed { index, badge ->
            points.getOrNull(index)?.let { center ->
                IntRect(IntOffset(center.x - badge.width / 2, center.y - badge.height / 2), IntSize(badge.width, badge.height))
            }
        }
        val occupied = badgeRects.filterNotNull().toMutableList()
        val labelRects = arrayOfNulls<IntRect>(markers.size)

        markers.indices.sortedBy { markers[it].priority }.forEach { index ->
            val badgeRect = badgeRects[index] ?: return@forEach
            val placement = placeLabel(
                badge = badgeRect,
                labelSize = IntSize(labels[index].width, labels[index].height),
                bounds = bounds,
                occupied = occupied,
                gap = gap,
                preferred = lastSides[index],
            )

            lastSides[index] = placement.side
            labelRects[index] = placement.rect
            occupied += placement.rect
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            badges.forEachIndexed { index, badge -> badgeRects[index]?.let { badge.place(it.topLeft) } }
            labels.forEachIndexed { index, label -> labelRects[index]?.let { label.place(it.topLeft) } }
        }
    }
}

/** 지나간 구간을 뺀 남은 경로와 마커가 모두 보이게 카메라를 맞춘다. */
private fun fitCamera(
    map: KakaoMap,
    routes: List<MapRouteLine>,
    markerCoordinates: List<Coordinate>,
    animation: CameraAnimation?,
) {
    val all = routes.flatMap { route ->
        route.progressFrom?.let { remainingPointsFrom(route.points, it) } ?: route.points
    } + markerCoordinates

    val update = when {
        all.size >= 2 -> CameraUpdateFactory.fitMapPoints(all.map { it.toLatLng() }.toTypedArray(), 0, FIT_MAX_ZOOM_LEVEL)
        all.size == 1 -> CameraUpdateFactory.newCenterPosition(all.first().toLatLng(), SINGLE_POINT_ZOOM_LEVEL)
        else -> return
    }

    if (animation != null) map.moveCamera(update, animation) else map.moveCamera(update)
}

private fun Coordinate.toLatLng(): LatLng = LatLng.from(latitude, longitude)

// 지도가 아직 좌표를 화면 점으로 바꾸지 못하면 null 이고, 그 마커는 그리지 않는다.
private fun List<MapMarkerSlot>.projectOn(map: KakaoMap): List<IntOffset?> = map { marker ->
    map.toScreenPoint(marker.coordinate.toLatLng())?.let { point -> IntOffset(point.x, point.y) }
}
