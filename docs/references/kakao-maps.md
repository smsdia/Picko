# 카카오 지도 참조

> 출처: https://apis.map.kakao.com/android_v2/ (확인일 2026-11-11). 요약이며 원문 시그니처는 사용 전에 확인한다.

## 설정

| 항목 | 값 |
| --- | --- |
| 좌표 | `com.kakao.maps.open:android:2.15.2` (`libs.kakao.maps`). 최신 릴리스는 저장소의 `maven-metadata.xml`로 확인한다 |
| 저장소 | `https://devrepo.kakao.com/nexus/repository/kakaomap-releases/` (`settings.gradle.kts`, `includeGroup("com.kakao.maps.open")`) |
| 초기화 | `PickoApplication`에서 `KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE)` |
| 키 | `local.properties`의 `kakao.native.key` ([SECURITY](../SECURITY.md)) |
| 키 해시 | 카카오 개발자 콘솔 → 플랫폼 → Android에 패키지명과 함께 등록한다. 디버그 해시는 `keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android \| openssl sha1 -binary \| openssl base64` |
| 권한 | `INTERNET` |

## 구현 (`presentation/pickup/component/RouteMap.kt`)

- `MapView`를 `AndroidView`로 감싼다. `start(MapLifeCycleCallback, KakaoMapReadyCallback)`로 시작하고 `ON_RESUME`/`ON_PAUSE`에 `resume()`/`pause()`, 해제할 때 `finish()`를 호출한다.
- 초기 카메라는 `KakaoMapReadyCallback`의 `getPosition()`/`getZoomLevel()`로 준다. 이후 이동은 `moveCamera(CameraUpdateFactory.fitMapPoints(LatLng[]))`(여러 점이 모두 보이게) 또는 `newCenterPosition(latLng, zoom)`.
- 인증 실패는 `MapLifeCycleCallback.onMapError`로 온다(키 오류, 키 해시 미등록 등).
- 지도는 Preview에서 그릴 수 없어 `LocalInspectionMode`일 때 건너뛴다.
- 카카오 SDK 타입은 `RouteMap.kt` 밖으로 내보내지 않는다. 호출하는 쪽은 `MapRouteLine`(좌표와 색)과 `MapMarkerSlot`(좌표와 Compose 내용)만 쓴다.

### 경로선

경로선은 이미 가진 좌표를 잇는 기능이고, SDK가 경로를 계산해 주지는 않는다. 아래 시그니처는 설치된 SDK(2.15.2)의 클래스에서 확인했다.

| 용도 | API |
| --- | --- |
| 스타일 | `RouteLineStyle.from(lineWidth: Float, lineColor: Int)` |
| 선분 | `RouteLineSegment.from(List<LatLng>, RouteLineStyle...)` |
| 추가 | `map.routeLineManager.layer.addRouteLine(RouteLineOptions.from(segment))` |
| 지우기 | `map.routeLineManager.layer.removeAll()` |

### 마커

카카오 라벨(POI)은 비트맵만 받고 벡터 드로어블은 `LabelStyle.from(context, resId)`로 그려지지 않았다(에뮬레이터 실측: 라벨은 추가되지만 보이지 않음). 그래서 마커는 라벨을 쓰지 않고 지도 위에 Compose를 겹쳐 그린다.

| 항목 | 내용 |
| --- | --- |
| 위치 | `map.toScreenPoint(LatLng)`(`Point?`)로 화면 좌표를 구해 `Modifier.offset`으로 놓는다 |
| 갱신 | 경로선과 마커를 바꿀 때와 `setOnCameraMoveEndListener`에서 다시 계산한다 |
| 제스처 | `setGestureEnable(GestureType, false)`로 카메라를 움직이는 제스처를 막는다. 겹친 마커가 지도와 어긋나지 않게 하려는 것이다 |
| 여백 | 마커가 잘리지 않도록 `setPadding`으로 가장자리 여백을 둔다 |

### 길찾기 (미연동)

경로 계산은 SDK에 없다. 카카오모빌리티 길찾기 API(`apis-navi.kakaomobility.com/v1/directions`, REST API 키를 `Authorization: KakaoAK` 헤더로 전달)가 있다. 검색으로 확인한 범위는 자동차 경로까지이고 도보 경로는 확인하지 못했다. 현재는 `RouteRepository`의 가짜 구현을 쓴다.
