package com.offcourse.picko.presentation.main.component

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.offcourse.picko.R
import com.offcourse.picko.presentation.main.model.PickupSpotUiModel

// 카카오 지도 줌 레벨. 클수록 넓게 보인다.
private const val PICKUP_ZOOM_LEVEL = 16

// ic_pickup_pin.xml 의 크기(dp)
private const val PIN_WIDTH_DP = 36
private const val PIN_HEIGHT_DP = 48

/**
 * 픽업 지점 하나를 마커(POI)로 보여주는 카카오 지도.
 * 카카오 SDK 타입은 이 파일 밖으로 내보내지 않는다.
 */
@Composable
fun PickupMap(
    spot: PickupSpotUiModel,
    onMapError: (Exception) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember(context) { MapView(context) }
    val currentSpot by rememberUpdatedState(spot)
    val currentOnMapError by rememberUpdatedState(onMapError)
    var kakaoMap by remember(mapView) { mutableStateOf<KakaoMap?>(null) }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )

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
                // 지도가 준비되기 전에 카메라가 처음부터 픽업 지점을 보게 한다.
                override fun getPosition(): LatLng =
                    LatLng.from(currentSpot.latitude, currentSpot.longitude)

                override fun getZoomLevel(): Int = PICKUP_ZOOM_LEVEL

                override fun onMapReady(map: KakaoMap) {
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

    LaunchedEffect(kakaoMap, spot) {
        val map = kakaoMap ?: return@LaunchedEffect
        val position = LatLng.from(spot.latitude, spot.longitude)

        map.labelManager?.layer?.let { layer ->
            layer.removeAll()
            layer.addLabel(
                LabelOptions
                    .from(position)
                    .setStyles(pinStyle(context)),
            )
        }

        map.moveCamera(CameraUpdateFactory.newCenterPosition(position, PICKUP_ZOOM_LEVEL))
    }
}

// 벡터 드로어블은 지도 SDK가 직접 그리지 못해서 비트맵으로 바꿔 넘긴다.
// 핀 끝(하단 중앙)이 좌표를 가리키도록 앵커를 둔다.
private fun pinStyle(context: Context): LabelStyles {
    val pin = checkNotNull(ContextCompat.getDrawable(context, R.drawable.ic_pickup_pin)) {
        "ic_pickup_pin 을 찾을 수 없다."
    }
    val density = context.resources.displayMetrics.density
    val bitmap = pin.toBitmap(
        width = (PIN_WIDTH_DP * density).toInt(),
        height = (PIN_HEIGHT_DP * density).toInt(),
    )

    return LabelStyles.from(
        LabelStyle
            .from(bitmap)
            .setApplyDpScale(false)
            .setAnchorPoint(0.5f, 1f),
    )
}
