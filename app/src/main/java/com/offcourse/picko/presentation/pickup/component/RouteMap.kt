package com.offcourse.picko.presentation.pickup.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntOffset
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
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.designsystem.theme.PickoTheme

// 카카오 지도 줌 레벨. 클수록 넓게 보인다.
private const val SINGLE_POINT_ZOOM_LEVEL = 16

// 가까운 점들을 맞출 때 이 레벨보다 더 확대하지 않는다. 차량이 픽업 지점에 가까워져도 지도가 과하게 커지지 않게 한다.
private const val FIT_MAX_ZOOM_LEVEL = 17

// 경로선 굵기(px)
private const val ROUTE_LINE_WIDTH = 16f

// 마커가 잘리지 않도록 지도 가장자리에 두는 여백(px)
private const val MAP_HORIZONTAL_PADDING = 160
private const val MAP_VERTICAL_PADDING = 110

// 마커 배지 중심에서 마커 왼쪽 위까지의 거리. PickoMapMarker 는 왼쪽 여백 12dp 에 배지 반지름 44dp 이고 높이 112dp 이다.
private val MarkerAnchor = 56.dp

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

/** 지도에 그릴 경로선. [points]는 순서대로 이어진다. 값으로 비교해 같은 경로면 지도를 다시 그리지 않는다. */
data class MapRouteLine(
    val points: List<Coordinate>,
    val color: Color,
)

/**
 * 지도 위 [coordinate]에 겹쳐 그릴 마커. [content]의 배지 중심이 좌표에 놓인다.
 * 배지가 마커의 오른쪽 끝에 있으면(라벨이 왼쪽) [isBadgeAtEnd]를 켠다.
 */
class MapMarkerSlot(
    val coordinate: Coordinate,
    val isBadgeAtEnd: Boolean = false,
    val content: @Composable () -> Unit,
)

/**
 * 경로선과 마커를 보여 주는 카카오 지도. 카메라는 경로선과 마커가 모두 보이도록 맞춘다.
 * 카카오 SDK 타입은 이 파일 밖으로 내보내지 않는다. Preview 에서는 지도를 그리지 못해 마커만 보인다.
 */
@Composable
internal fun RouteMap(
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

    val initialCenter = routes.firstOrNull()?.points?.firstOrNull() ?: markers.firstOrNull()?.coordinate

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )

        markers.forEachIndexed { index, marker ->
            val point = markerPoints.getOrNull(index) ?: return@forEachIndexed

            Box(
                modifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val anchor = MarkerAnchor.roundToPx()
                    // 배지 중심이 마커 왼쪽 끝에서 anchor 만큼, 배지가 오른쪽 끝이면 오른쪽 끝에서 anchor 만큼 떨어져 있다.
                    val x = if (marker.isBadgeAtEnd) point.x - (placeable.width - anchor) else point.x - anchor

                    layout(placeable.width, placeable.height) {
                        placeable.place(x, point.y - anchor)
                    }
                },
            ) {
                marker.content()
            }
        }
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
                    map.setOnCameraMoveEndListener { movedMap, _, _ ->
                        markerPoints = currentMarkers.projectOn(movedMap)
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
    val routeLines = remember(mapView) { mutableListOf<RouteLine>() }

    LaunchedEffect(kakaoMap, drawableRoutes, markerCoordinates) {
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

        val all = routes.flatMap { it.points } + markers.map { it.coordinate }

        when {
            all.size >= 2 -> map.moveCamera(
                CameraUpdateFactory.fitMapPoints(all.map { it.toLatLng() }.toTypedArray(), 0, FIT_MAX_ZOOM_LEVEL),
            )
            all.size == 1 -> map.moveCamera(CameraUpdateFactory.newCenterPosition(all.first().toLatLng(), SINGLE_POINT_ZOOM_LEVEL))
        }

        markerPoints = markers.projectOn(map)
    }
}

private fun Coordinate.toLatLng(): LatLng = LatLng.from(latitude, longitude)

// 지도가 아직 좌표를 화면 점으로 바꾸지 못하면 null 이고, 그 마커는 그리지 않는다.
private fun List<MapMarkerSlot>.projectOn(map: KakaoMap): List<IntOffset?> = map { marker ->
    map.toScreenPoint(marker.coordinate.toLatLng())?.let { point -> IntOffset(point.x, point.y) }
}
