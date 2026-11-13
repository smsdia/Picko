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
- 지나간 경로는 경로선을 다시 올리지 않고 `progressTo`로 지운다. `MapRouteLine.progressFrom`에 차량 위치를 주면 `RouteMap`이 `getProgressFromPoint`로 progress를 구해 900ms 동안 지운다. 에뮬레이터에서 progress가 차량 이동 거리에 맞게 단조 증가하는 것을 로그로 확인했다(4초에 0.0108 ≈ 160m ÷ 약 15km). 경로를 벗어난 위치에서의 동작은 확인 전이다.
- 경로선은 선의 개수가 같으면 `changeSegments`로 좌표만 바꾼다. 지우고 다시 그리면 깜빡인다. 효과의 키도 값(`MapRouteLine`은 data class, 마커는 좌표 목록)으로 둔다. 호출마다 새로 만든 리스트를 키로 쓰면 리컴포지션마다 지도를 다시 그린다.
- 카메라는 경로 모양이 바뀔 때 한 번, 그 뒤에는 10초마다(`CAMERA_REFIT_INTERVAL_MILLIS`) 지나간 구간을 뺀 남은 경로(`remainingPointsFrom`)와 마커 좌표로 `fitMapPoints(points, padding=0, maxZoomLevel=17)`로 다시 맞춘다. `padding`은 화면 가장자리에서 안쪽으로 들이는 px이고, 마커 배지가 잘리지 않는 여백은 `setPadding`이 맡는다. `maxZoomLevel`은 가까운 점들에서 과하게 확대하지 않게 하는 상한이다(공식 레퍼런스 `CameraUpdateFactory`, 확인일 2026-11-13).
- 카카오 SDK 타입은 `RouteMap.kt` 밖으로 내보내지 않는다. 호출하는 쪽은 `MapRouteLine`(좌표, 색, 지나간 구간을 지울 위치)과 `MapMarkerSlot`(좌표, 배지, 라벨, 우선순위)만 쓴다.

### 경로선

경로선은 이미 가진 좌표를 잇는 기능이고, SDK가 경로를 계산해 주지는 않는다. 아래 시그니처는 설치된 SDK(2.15.2)의 클래스에서 확인했다.

| 용도 | API |
| --- | --- |
| 스타일 | `RouteLineStyle.from(lineWidth: Float, lineColor: Int)` |
| 선분 | `RouteLineSegment.from(List<LatLng>, RouteLineStyle...)` |
| 추가 | `map.routeLineManager.layer.addRouteLine(RouteLineOptions.from(segment))` |
| 지우기 | `map.routeLineManager.layer.removeAll()` |
| 지나간 구간 지우기 | `RouteLine.getProgressFromPoint(LatLng)`로 위치의 progress(0.0~1.0)를 구해 `RouteLine.progressTo(progress, durationMillis)`. 0.0~1.0이면 경로 시작점에서부터 부드럽게 사라진다 ([가이드](https://apis.map.kakao.com/android_v2/docs/api-guide/routeline/), 확인일 2026-11-13). `setProgress(progress)`는 애니메이션 없이 바로 적용 |
| 좌표만 바꾸기 | `RouteLine.changeSegments(RouteLineSegment...)` (`addRouteLine`이 돌려주는 `RouteLine`) |

### 마커

카카오 라벨(POI)은 비트맵만 받고 벡터 드로어블은 `LabelStyle.from(context, resId)`로 그려지지 않았다(에뮬레이터 실측: 라벨은 추가되지만 보이지 않음). 그래서 마커는 라벨을 쓰지 않고 지도 위에 Compose를 겹쳐 그린다.

| 항목 | 내용 |
| --- | --- |
| 위치 | `map.toScreenPoint(LatLng)`(`Point?`)로 화면 좌표를 구해 `Modifier.offset`으로 놓는다 |
| 갱신 | 경로선과 마커를 바꿀 때와 `setOnCameraMoveEndListener`에서 다시 계산한다 |
| 제스처 | `setGestureEnable(GestureType, false)`로 카메라를 움직이는 제스처를 막는다. 겹친 마커가 지도와 어긋나지 않게 하려는 것이다 |
| 라벨 자리 | 배지는 화면 좌표에 중심을 맞춰 놓고, 라벨은 `MapMarkerSlot.priority`가 작은 마커부터 오른쪽, 왼쪽, 위, 아래 순서로 지도 안에 들어오고 다른 배지·라벨과 겹치지 않는 첫 자리에 놓는다(`placeLabel`). 한 번 정한 쪽은 아직 맞으면 바꾸지 않는다. 어디도 맞지 않으면 오른쪽 자리를 지도 안으로 밀어 넣는다 |
| 이동 | 마커 위치는 카메라가 멈춰 있어도 좌표가 바뀔 때마다 다시 구하고, 900ms 동안 선형으로 미끄러뜨린다. 카메라가 움직이는 동안과 직후에는 지도에 붙도록 바로 옮긴다 |
| 여백 | 마커가 잘리지 않도록 `setPadding`으로 가장자리 여백을 둔다 |

### 길찾기 (차량 경로만 연동)

경로 계산은 SDK에 없다. 카카오모빌리티 길찾기 API(`apis-navi.kakaomobility.com/v1/directions`, REST API 키를 `Authorization: KakaoAK` 헤더로 전달)가 있다. 공식 시작 가이드(`developers.kakaomobility.com/guide/navi-api/start.html`, 확인일 2026-11-13)에 있는 길찾기는 자동차 5종(자동차 경유지 5개, 다중 경유지 30개, 다중 출발지, 다중 목적지, 미래 교통)뿐이고 **도보는 없다**. 사용은 카카오 디벨로퍼스에 앱을 등록하고 REST API 키를 쓰는 방식이다.
- 무료 쿼터(요금 페이지 `developers.kakaomobility.com/price/`, 확인일 2026-11-13): 자동차 길찾기 하루 10,000건, 다중 경유지 5,000건, 다중 출발지와 다중 목적지 각 1,000건. 초과분은 자동차 길찾기 건당 8원(월 100만 건까지), 다중 경유지 16원, 다중 목적지 20원이다. 쿼터 상향이나 제휴는 `api.partners@kakaomobility.com`으로 문의한다.
- 상업 이용 시 무료 쿼터 안이어도 사전 제휴가 필요하다는 안내는 검색 결과(카카오 데브톡)에서만 봤고 위 공식 페이지에서는 확인하지 못했다. 제휴 문의로 확인한다.
- 연동: `data/route`의 `KakaoDirectionsDataSource`가 `GET /v1/directions`를 `HttpURLConnection`으로 부른다(새 의존성 없음). 헤더는 `Authorization: KakaoAK <REST 키>`이고 키는 `local.properties`의 `kakao.platform.key`(`BuildConfig.KAKAO_PLATFORM`)다. `origin`/`destination`은 `경도,위도` 순서다. 응답의 `routes[0].sections[].roads[].vertexes`(경도, 위도가 번갈아 놓인 1차원 배열)를 이어 경로선을 만들고, `result_code`가 0이 아니면 실패로 본다(`KakaoDirectionsDto.toRouteModel`).
- 도보 길찾기(`/affiliate/walking/v1/directions`)는 제휴 파트너 전용이고 사전 제휴 계약이 필요하다(공식 페이지 `developers.kakaomobility.com/affiliate/walking/directions`, 확인일 2026-11-13). 그래서 탑승자 도보 경로와 목적지 경로는 `FakeRouteRepository` 값을 그대로 쓴다.
- 호출이 실패하거나 키가 없으면 차량 경로도 가짜 값으로 대신한다(`KakaoRouteRepository`).
