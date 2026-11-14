# Pleos SDK 참조 색인

> 출처: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/ (확인일 2026-11-08)
> 이 파일은 원문 복사가 아니라 **요약**이다. 문서 저작권은 Hyundai Motor Group에 있다. 구현 전에 해당 원문 페이지를 열어 시그니처를 확인한다. 요약에 없는 API는 존재한다고 가정하지 않는다.

## SDK 목록 (Picko 관련도)

| SDK | 버전(확인 시점) | 용도 | 관련도 | 원문 |
| --- | --- | --- | --- | --- |
| FusedLocation | v1.1.3 | 차량 위치 | 핵심 | `/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro` |
| NaviHelper | v3.0.5 | 목적지·경로·주행 상태 | 핵심 | `/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro` |
| SpeechToText | v3.3.1 | 음성 인식 | 핵심 | `/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro` |
| TextToSpeech | v3.3.1 | 음성 합성 | 핵심 | `/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro` |
| Vehicle | SDK v2.3.2 / Service v2.3.21 | 차량 정보 조회·제어 | 차량 정보 안내용 | `/docs/pleos-only/connect-sdk-pleos/Vehicle/intro` |
| Gleo Interface | - | Gleo AI 음성 명령 연동(App Schema) | 핵심 | `/docs/pleos-only/app-integration-guide/guide/core-concepts` |

## FusedLocation

- 권한: `pleos.car.permission.FUSED_LOCATION`
- 진입점 `FusedLocation`: `isSupported()`, `initialize()`, `getLastKnownLocation()`, `registerFusedLocationCallback()`, `unregisterFusedLocationCallback()`, `addProximityAlert()`, `removeProximityAlert()`, `showLocationAccessBlockedPopup()`, `release()`
- 모델 `LocationData`: 위도, 경도, 고도, 방위(bearing), 정확도, 타임스탬프
- 상세: `.../FusedLocation/project-setup`, `.../entry`, `.../declaring-permissions`

## NaviHelper

- 좌표 `ai.pleos.playground:NaviHelper:3.0.5`(AAR의 클래스를 `javap`로 확인한 시그니처. 문서 본문에는 시그니처가 없다). 권한 `pleos.car.permission.NAVI_ROUTE`(경로 정보·요청), `NAVI_ROUTE_SEARCH`(탐색·취소·경유지), `NAVI_CUSTOM_MAP`(내비 화면 요청)
- 진입점 `ai.pleos.playground.navi.helper.NaviHelper(context)`. 사용 순서: 생성 → `initialize(onConnected, onFailed)` → `onConnected`에서 `addListener(NaviHelperEventListener)` → API 호출 → 결과는 콜백 → `removeListener()`, `release()`
- 호출: `requestRoute(RouteInfo)`(목적지 설정과 경로 요청을 겸함), `cancelRoute()`, `changeRouteOption(RouteOption)`, `requestReRoute()`, `requestRouteWithWaypoints`, `addWaypoint`, `removeWaypoint`, `requestTrip(TripRequest)`(안내 시작 없이 거리·도착 예정), `requestNavigationScreen(NavigationScreenRequest)`(`Foreground`, `OverviewRoute`, `EditRoute`, `SearchPlace`, `POIDetailPopup` 등), 조회 `getRouteStateInfo`, `getDestinationInfo`, `getTBTInfo`, `getCurrentLocationInfo`
- 결과는 `NaviHelperEventListener` 콜백이다. `onRouteStarted(RouteStartInfo: duration, distance)`, `onRoutingCompleted`, `onTripInfo(TripInfo: distance, eta)`, `onDestinationInfo`, `onTBTInfo`, `onNavigationStatus`, `onError(NaviErrorCode)`. `requestRoute()`가 성공해도 탐색 완료가 아니다.
- `NaviErrorCode`(`ai.pleos.playground.navi.constants`, 소스는 `navi-data` 3.0.4)에는 `CAUSE_LOW_BATTERY`, `CAUSE_SEARCH_ROUTE_OVER_DISTANCE`, `CAUSE_SEARCH_ROUTE_TOO_SHORT_DISTANCE` 등이 있다. 우리 앱은 `requestRoute` 뒤 3초 동안 `onError`를 기다려 이 셋과 그 밖의 실패를 안내 결과로 바꾼다(3초는 측정값이 아닌 추정). 에뮬레이터(2026-11-13)에서 정상 경로는 오류 없이 내비가 경로 선택 화면으로 앱 영역을 차지했고, 배터리를 3%로 낮춰도(`PUT /vehicle/ev-battery?field=soc&value=3`, 콘솔) `CAUSE_LOW_BATTERY` 오류 없이 안내가 시작돼 오류 대화상자는 재현하지 못했다. 내비가 앱 영역을 차지한 동안 우리 앱의 대화상자가 보이는지도 확인 전이다.
- **경로 좌표(폴리라인)를 돌려주는 API는 없다.** `TripSummary.routePoints`는 출발·경유·도착 지점뿐이다. 그래서 `RouteMap`처럼 앱이 선을 그리려면 다른 경로 소스가 필요하다.
- 차량 도착 시간 갱신(2026-11-13 에뮬레이터): `onDrivingInfo`는 안내 중에만 오고 약 5분 간격이라 쓰지 않는다. 픽업 이동 중에는 `requestTrip`을 10초 간격으로 다시 불러 갱신한다(호출 하나 약 0.3초, 내비 앱이 실행 중이어야 응답하고 아니면 타임아웃). 위치가 움직일 때 값이 줄어드는지는 에뮬레이터에서 확인하지 못했다.
- 앱 연동: `data/navi`(`NaviHelperRepository`)가 `requestTrip`(소요 시간 미리 보기)과 `requestRoute`(안내 시작)만 쓴다. 안내를 시작하면 앱은 흐름을 끝내고 안내는 내비가 맡는다. 권한 `NAVI_ROUTE`, `NAVI_ROUTE_SEARCH`는 메인 매니페스트에 있다.
- 에뮬레이터에서 `requestTrip` 값이 이동에 따라 줄지 않는 이유(2026-11-13): 내비 엔진이 자기 위치를 고정값(경도 127.094681, 위도 37.4127212, 위성 0개, 속도 0)으로 두고 `requestTrip`의 출발지로 쓴다(`navi_engine mapmatching` 로그와 `grpc_caller` 요청의 `origin`). 에뮬레이터가 만드는 NMEA는 체크섬이 틀려 내비 앱의 `GpsLocationSource`가 `Invalid NMEA message`로 거부하고, 체크섬을 맞춘 NMEA를 `adb emu geo nmea`로 1초마다 보내 경고는 없앴지만 내비 위치와 ETA(30분, 12,463m)는 그대로였다. 그래서 GPX 재생이나 `geo fix`로는 내비 값의 감소를 확인할 수 없고 실차에서 봐야 한다.
- 동작 확인용 디버그 화면: `app/src/debug/.../debug/navi/NaviExperimentActivity`(디버그 빌드의 "Navi 실험" 런처 아이콘). 에뮬레이터(Connect v2.1.0, 2026-11-13) 확인: `requestRoute`와 `requestNavigationScreen(Foreground)`는 내비 앱(`ai.umos.maps.android.navigation.app`)을 **오른쪽 앱 영역에 띄우고 우리 앱을 가린다**(홈 화면 앱이 `hideTask`). 왼쪽 차량 뷰(`ai.umos.drivingview`)는 바뀌지 않고, 앱이 옮길 수 없다(`cmd activity task resize`는 shell 권한이라 일반 앱은 못 쓰고, 해도 화면 위치는 홈 화면 앱이 정한다). 오른쪽 영역 가장자리의 ⋮ 핸들을 사용자가 끌면 홈 화면 앱이 내비와 우리 앱을 좌우로 나눠 둘 다 보이고, 이 상태에서는 우리 앱이 가려지지 않은 채 `requestRoute`, `OverviewRoute`가 동작한다. 분할을 앱이 코드로 켜는 방법은 확인하지 못했다. 공식 문서(앱 구조·화면 모델, 창 정책)에도 앱이 분할을 요청하는 API는 없고 창 배치는 사용자 조작(세 손가락 스와이프, 창 막대)이다. 우리 앱이 단독일 때 `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_LAUNCH_ADJACENT`로 내비 앱을 시작해 봤으나 무시되고 슬롯이 2:0이 돼 내비가 앱 영역을 점거했다. 사용자가 우리 앱과 전화 앱을 분할해 둔 상태에서 `requestRoute`를 부르면 내비가 **상대 슬롯의 앱(전화)을 가리고 그 자리에 들어가며** 우리 앱은 그대로 보인다(2026-11-13 에뮬레이터).
- 창 배치(2026-11-13 에뮬레이터, 슬롯 비율은 홈 화면 앱 로그 `updateSlotWeight`): 우리 앱이 단독일 때 내비를 `startActivity`(플래그 없음 또는 `LAUNCH_ADJACENT`)로 띄우면 **2:0**이고 내비가 앱 영역 전체를 차지해 우리 앱이 가려진다. 1:1이 되는 것은 사용자가 먼저 분할해 둔 경우뿐이다. 우리 액티비티에 `screenOrientation="portrait"`를 줘도 반쪽 창이 되지 않고 가로 영역 가운데에 레터박스로 놓이며, 이어서 내비를 띄워도 2:0이다.
- 안내 중 정보(에뮬레이터 확인): `requestRoute` 뒤 안내가 시작되면 앱이 폴링 없이 `onRouteStarted`, `onNavigationStatus(ROUTE_DRIVE)`, `onTBTInfo`, `onDrivingInfo`(목적지 이름·남은 시간·거리)를 받는다. `getDestinationInfo()`는 `remainDistance`, `remainTime`을, `getCurrentLocationInfo()`는 위도·경도·방향·주소를 준다. 에뮬레이터 차량이 정지 상태라 값이 줄어드는 것과 `onDrivingInfo`의 갱신 주기는 확인하지 못했다(내비 앱 자체 로그에서는 5분 간격으로 보였다).
- 참고: 안내 취소 뒤 앱 복귀(2026-11-13 에뮬레이터, 앱은 쓰지 않는다): `cancelRoute`를 부르면 내비 앱이 스스로 앞으로 올라와 우리 앱이 백그라운드가 되고, 이후 `ActivityManager.AppTask.moveToFront()`는 `Background activity launch blocked`로 조용히 막힌다. 앱이 `SYSTEM_ALERT_WINDOW`(사용자가 설정에서 허용하는 특수 권한)를 허용받으면 통과해 우리 앱이 앞으로 돌아온다(`adb shell appops set --user 10 <패키지> SYSTEM_ALERT_WINDOW allow`로 확인). 내비 앱은 이 권한으로 우리 앱 위에 올라온다. 권한 없이는 사용자가 GNB의 앱 아이콘으로 돌아와야 한다.
- 홈 화면 앱(`ai.umos.homescreen`)의 외부 진입점(`dumpsys`로 확인): 동적 브로드캐스트 수신기 `ai.umos.intent.action.ENFORCE_APP_RATIO`(`CONTROL_WINDOW` 필요, signature|privileged라 일반 앱은 못 씀), `ai.umos.intent.action.HIDE_APP`(`MANAGE_TASK`, normal), `ai.umos.homescreen.intent.action.REQUEST_TO_ENFORCE_APP_ORIENTATION`(권한 요구 없음, `handleEnforceAppOrientationRequest`가 호출됨, extra 키는 확인하지 못했다. 문서에 없고 APK를 읽을 수 없다). 내비 앱도 `ENFORCE_APP_ORIENTATION` 권한을 갖는다. 분할을 켜는 인터페이스는 찾지 못했다. `requestRoute`는 경로 선택 화면을 띄우고 몇 초 뒤 안내가 자동 시작된다. `requestTrip`은 화면을 바꾸지 않고 `onTripInfo`(거리 12463m, eta 2399초 형태)만 준다.

## SpeechToText

- 좌표 `ai.pleos.playground:SpeechToText:3.3.1` (Pleos Maven). 이 라이브러리가 `ai.pleos.playground.security:universal-access:0.6.0`을 의존해서, 저장소 필터는 `ai.pleos.playground`의 하위 그룹까지 허용해야 한다(`settings.gradle.kts`).
- 권한: `pleos.car.permission.STT_SERVICE` (런타임 권한은 문서에 없다). 선언하지 않으면 초기화나 호출이 실패할 수 있다.
- 진입점 `ai.pleos.playground.stt.SpeechToText`, 생성 예: `SpeechToText(applicationContext, Mode.HYBRID)`. 패키지: `stt.constant`(`Mode`, `ErrorType`), `stt.listener`(`ResultListener`, `OnInitializeListener`, `OnServerConnectionListener`)
- 사용 순서: 생성 → `initialize(OnInitializeListener)` → `registerApp(clientID, clientSecret, OnServerConnectionListener)` → `onConnected()` 뒤에 `setLocale(Locale.KOREA)`, `addListener(ResultListener)`, `request()` → 끝나면 `stop()`, `removeListener()`, `release()`. `release()` 뒤에 다시 쓰려면 새 객체를 만든다. `setLocale`은 `request()` 전에 한다.
- `Mode`: HYBRID(기본, 서버 우선, 앱 등록 필수), SERVER(앱 등록 필수), ON_DEVICE(앱 등록 선택). 클라이언트 ID/시크릿은 Pleos Playground My Project에서 발급한다. 우리 앱은 `ON_DEVICE`만 쓰고 `registerApp`을 부르지 않는다. 에뮬레이터(2026-11-13)에서 `createStt ON_DEVICE`로 연결되고 `ko_KR` 로케일로 오디오 처리(`recognize` 카운트 증가)가 도는 것을 로그로 확인했다. 호스트 `say`로 재생한 한국어 음성은 에뮬레이터 마이크로 들어가지 않아 인식 품질은 확인하지 못했다.
- `ResultListener`: `onReady()`, `onStartedRecognition()`, `onUpdated(stt: String, completed: Boolean)`, `onUpdatedEpdData(on, off)`, `onUpdatedRms(rms)`, `onEndedRecognition()`, `onError(ErrorType)`. 신뢰도 필드는 없다. `request()`가 마이크를 켠다.
- `ErrorType`: AUDIO_ERROR, INVALID_REQUEST_ERROR, MIC_ERROR, NETWORK_ERROR, NOT_SUPPORTED_API, ON_DEVICE_ENGINE_ERROR, PERMISSION_DENIED, SERVER_INTERNAL_ERROR, TIMEOUT, UNAUTHENTICATED, UNAVAILABLE_LANGUAGE, UNKNOWN
- 음성을 텍스트로만 바꾼다. 사람과 장소 같은 값은 앱이 문장에서 읽어야 한다(`SpokenPickupParser`).
- 에뮬레이터 확인(2026-11-13): 홈 음성 대기 화면에서 `initialize` → `registerApp`(인증 통과) → `setLocale ko_KR` → `request` → 마이크 녹음 시작까지 로그로 확인했다. 최종 결과(`onUpdated`의 `completed=true`)와 `onEndedRecognition`의 순서는 아직 확인하지 못했다.
- 문서에 없는 것: 무음 자동 종료 시간(앱이 15초 안에 끝나지 않으면 직접 끝낸다), `onUpdated`의 `completed`와 `onEndedRecognition`의 호출 순서.

## TextToSpeech

- 좌표 `ai.pleos.playground:TextToSpeech:3.3.1` (Pleos Maven). 권한 `pleos.car.permission.TTS_SERVICE` (런타임 권한은 문서에 없다).
- 진입점 `ai.pleos.playground.tts.TextToSpeech`, 생성 `TextToSpeech(context, Mode)`. 패키지: `tts.constant`(`Mode`, `ErrorType`), `tts.listener`(`EventListener`, `OnInitializeListener`, `OnServerConnectionListener`)
- 사용 순서: 생성 → `initialize(OnInitializeListener)` → (서버 모드만 `registerApp`) → `setLocale(Locale.KOREA)`, `addEventListener(EventListener)` → `speak(text, isFinal = true)` → 끝나면 `stop()`, `removeEventListener()`, `release()`. `release()` 뒤에 다시 쓰려면 새 객체를 만든다.
- `Mode`: HYBRID(서버 우선, 서버를 쓰려면 앱 등록 필요), SERVER(앱 등록 필수), ON_DEVICE(등록 불필요). 우리 키는 서버 STT가 `PERMISSION_DENIED`라서 서버 합성도 막힐 수 있어 온디바이스 모드를 쓴다.
- `EventListener`: `onReady()`, `onStart()`, `onUpdatedRms(rms)`, `onDone()`(정상 종료), `onStop()`(중지), `onError(ErrorType)`. 여러 조각을 한 발화로 읽으려면 앞 조각을 `isFinal = false`로 보낸다.
- `ErrorType`은 STT와 같은 12종이다(AUDIO_ERROR, INVALID_REQUEST_ERROR, MIC_ERROR, NETWORK_ERROR, NOT_SUPPORTED_API, ON_DEVICE_ENGINE_ERROR, PERMISSION_DENIED, SERVER_INTERNAL_ERROR, TIMEOUT, UNAUTHENTICATED, UNAVAILABLE_LANGUAGE, UNKNOWN).
- 에뮬레이터 확인(2026-11-13): 온디바이스 모드에서 `initialize` → `setLocale ko_KR` → `speak`가 화자 `seoyoon`으로 합성해 약 5초 뒤 `onDone`이 왔고 `release`까지 정상이다. 실제 소리는 확인하지 못했다.
- 문서에 없는 것: 오디오 출력 위치(`setAudioZoneId`, `setAudioAttributes`)와 화자(`setSpeakerType`)의 값. 기본값을 쓴다.

## Vehicle

- 차량 통신은 IPC라 capability·get·set 결과가 비동기(`onComplete`, `onFailed`)로 전달된다.
- 진입점 `Vehicle`: `initialize()`, `release()`. 도메인 객체는 `getDoor()`, `getBrake()` 등으로 얻는다. API 종류: `checkXXXCapabilities`, Get, Set, Callback
- 20개 이상 도메인(CarInfo, Door, Tire, Window, DrivingMode, Odometer, EvBattery, HVAC, Seat, Brake, Warning, Light, TurnSignal 등). 속도·기어 도메인은 요약에서 확인되지 않았다.
- 도메인별 권한: `.../Vehicle/vehicle-permission`. 기능 지원 확인: `.../Vehicle/check-capability`
- 샘플 앱: `/docs/pleos-only/tutorial/vehicle-sdk-sample-app`
- 주야간·조도(`Vehicle` 2.3.2 AAR를 `javap`로 확인, 2026-11-13): 주야간 신호(`NIGHT_MODE` 등)와 조도(lux) 값은 없다. `Display`는 온도 단위만 다룬다. 외부 조명 켜짐은 `Light`의 `getCurrentLowBeamLightState` 등과 `registerLowBeamLightState(LightStateListener)`(`onLightStateUpdated(LightState)`)로 받고, `LightState`는 `OFF`, `ON`, `DAYTIME_RUNNING`이다. 권한은 읽기 `pleos.car.permission.CAR_LIGHTS`, 쓰기 `CONTROL_CAR_LIGHTS`. `AmbientLight*`는 실내 무드등이라 환경 조도가 아니다. 아직 앱 의존성에 넣지 않았다.
- 주야간 일반 안드로이드 경로(에뮬레이터 Connect v2.1.0, 2026-11-13): `UiModeManager.currentModeType`이 `CAR`이고, `adb shell cmd uimode night yes`로 바꾸면 `Configuration.uiMode`와 `isSystemInDarkTheme()`이 따라온다. 차량 쪽에서 이 값을 자동으로 바꾸는지(`nightMode=AUTO`일 때의 동작)는 확인하지 못했다. `TYPE_LIGHT` 센서는 있으나 0.0 lux를 1회만 준다. 확인용 디버그 화면: `app/src/debug/.../debug/light/LightExperimentActivity`(런처 아이콘 "조도 실험").

## Gleo Interface

- 앱이 정의한 function call을 LLM이 생성하고, 앱의 응답을 바탕으로 최종 답변을 만든다.
- 앱은 **App Schema**(호출 가능한 기능을 정의한 JSON)를 등록해야 한다. 두 경로: Gleo Actions(AI가 앱 호출), Callbacks(앱이 응답)
- 상세: `.../app-integration-guide/guide/app-capability`, `.../action_callback`, `.../examples`, `.../deliver-schema`

## 의존성과 샘플

- Maven 저장소(샘플 앱 `settings.gradle.kts` 기준): `https://nexus-playground.pleos.ai/repository/maven-releases/` (샘플에는 인증 설정이 없다)
- FusedLocation 좌표: `ai.pleos.playground:FusedLocation:1.1.3`
- 권한 선언: `<uses-permission android:name="pleos.car.permission.FUSED_LOCATION" />`
- 공식 샘플: https://github.com/Pleos-Playground/pleos-sample-fusedlocation-app (확인일 2026-11-08). 구조: `FusedLocationClient`(SDK 래퍼) → `MainViewModel` → `MainScreen`. 샘플 빌드 설정은 AGP 8.8.2, Kotlin 2.0.0, compileSdk 35, minSdk 24이며 우리 프로젝트 버전과 다르다. 의존성을 붙일 때 호환을 직접 확인한다.
- FusedLocation 사용 흐름: `isSupported()` 확인 → 권한 요청 → `initialize()` → `registerFusedLocationCallback(FusedLocationListener)` 또는 `getLastKnownLocation(listener)` → 종료 시 `unregisterFusedLocationCallback`, `release()`
- 권한 `pleos.car.permission.FUSED_LOCATION`은 `dangerous`라 매니페스트 선언만으로는 `SecurityException: need Permission`이 난다. 런타임 허용이 필요하다(에뮬레이터 `pm grant`로 확인). `LocationData`는 위도, 경도, 고도, 방위(heading), `confidence`(에뮬레이터에서 GPS 정확도 m와 같은 값), 시각이다. 에뮬레이터는 `adb emu geo fix` 좌표를 1초마다 전달한다.
- `showLocationAccessBlockedPopup()`(소스 `FusedLocation-1.1.3-sources.jar`와 에뮬레이터 2026-11-13 확인): 넘겨받은 `Context`로 `ai.umos.intent.action.SENSOR_PRIVACY_DIALOG`(extra `sensor=3`)를 `startActivity`한다. `initialize()` 없이 부를 수 있고, `Activity` 컨텍스트가 필요하다. 에뮬레이터에서는 "위치정보 사용을 허용할까요?"라는 시스템 팝업이 `취소`/`허용` 버튼과 함께 뜨고, `허용`을 누르면 `location_mode`가 0에서 3으로 바뀐다. 안내만 하는 팝업이 아니라 위치를 켜 준다.
- SDK는 개인정보 보호 설정으로 막힌 상태를 따로 알리지 않는다(`FusedLocationListener.onError`는 일반 `Exception`뿐). 에뮬레이터에서 `adb shell settings put secure location_mode 0`이면 콜백이 한 번도 오지 않아 우리 앱의 5초 상한이 "받지 못함"으로 처리했다. 복구는 `location_mode 3`이다.
- 리스너: `FusedLocationListener`(`onChanged(location: LocationData?)`, `onError(exception)`), `ProximityAlertListener`(`onRegistered(alertId)`, `onTransition(event)`, `onError(exception)`). 패키지: `ai.pleos.playground.location.fused.*`
- 근접 알림: `addProximityAlert(ProximityAlertRequest, listener)`, `removeProximityAlert(alertId)`, 이벤트 `ProximityAlertEvent`(ENTER/EXIT)
- 에뮬레이터 테스트: 앱 실행 후 `adb emu geo fix <경도> <위도>`(경도가 먼저). 목 위치는 `adb shell appops set --user current <패키지> android:mock_location allow` 후 `LocationManager` 테스트 프로바이더로 주입한다.
- Vehicle SDK는 시작 가이드에서 Pleos Maven 저장소로 가져오며(문서상 2.3.1), 에뮬레이터는 Android SDK API 34, 프로젝트 minSdk 28 이상, RAM 4GB, Cold boot가 필요하다.
- 서버 연결이 필요한 모드(TextToSpeech, SpeechToText의 HYBRID·SERVER)는 콘솔의 클라이언트 ID/시크릿이 필요하다. 온디바이스 모드는 필요 없다.

## Gleo Interface 상세

- Gleo AI가 부팅할 때 `AssetManager`로 모든 앱의 `assets/schema.json`을 읽어 DB에 넣는다. 갱신은 앱 설치·삭제 시(ADB 또는 앱 마켓)다. Gleo 팀이 가이드라인 준수를 검토한 뒤 연동이 활성화된다.
- `schema.json` 필드: `packageName`, `appDescription`, `version`, `actionCalls`(**최대 3개**)
- 함수 정의: `type`는 `"function"` 고정, `function.name`은 UPPER_SNAKE_CASE(2~3단어), `description`은 영어 한 문장, `component`는 `"activity"`(전면 UI) 또는 `"service"`(백그라운드), `parameters`는 JSON Schema 객체. 파라미터 타입은 string, number, boolean, array 중 하나이고 enum은 문자열만 가능하다. `required`에는 꼭 필요한 것만 둔다.
- 수신: 함수 이름과 같은 intent action을 `AndroidManifest.xml`의 `intent-filter`에 선언한다 (category `DEFAULT`).
- 파라미터 수신: function 파라미터는 JSON 문자열로 `ACTION_PARAM` extra에 담긴다 (`intent.getStringExtra("ACTION_PARAM")`). `component`가 `activity`면 `startActivity`, `service`면 `sendBroadcast`로 전달된다. (원문 examples 페이지 요약 기준)
- Callback 응답: `ai.pleos.playground.intent.action.APP_ACTION_RESPONSE` 브로드캐스트에 extra `packageName`, `response`(JSON: `functionName`, `status`(success/asking/fail), `message`, 선택 `data`, `mode`). 아직 구현하지 않았고, 실제 연결을 시작할 때 구현한다. 구현 전에 원문(`.../action_callback`)으로 형식을 다시 확인한다. 콜백이 없으면 Gleo는 `actionCallback`을 `fail`로 서버에 보고한다 (action 이름이 틀려 앱이 실행되지 않은 경우에 확인했고, 콜백이 없을 때의 동작은 아직 확인하지 못했다).
- 에뮬레이터(Connect v2.1.0) 상태 (2026-11-10): 처음에는 호출어 모델 파일이 없어 `SpeechRecognitionConfig.dat ErrorNotExists`가 반복됐고, AVD Wipe Data 후 첫 부팅에서는 이 에러가 사라지고 음성 모델(`connect-cas` 1.17.1)이 설치됐다. 그래도 `getServerType`이 null이고 PTT 키(`KEYCODE_VOICE_ASSIST`)는 `android.speech.action.WEB_SEARCH`를 처리할 앱이 없어 실패(-91)해서 실제 음성 호출은 확인하지 못했다. 대신 `adb shell am start -a com.offcourse.picko.REQUEST_PICKUP -n com.offcourse.picko/.presentation.main.MainActivity --es ACTION_PARAM '{...}'`로 같은 intent를 보내 앱 쪽 수신을 검증한다.
- 공식 사용 사례(라디오 앱, 확인일 2026-11-10): 함수 3개(`SWITCH_BAND` service, `SEARCH_STATION` activity, `PLAY_FREQUENCY` service)를 두고, 수신부에서 `intent.action`으로 분기해 `ACTION_PARAM` JSON을 파싱한다. 필수 파라미터는 실행 전에 검증한다. 응답 status는 `success`(결과 데이터), `fail`(오류 메시지), `asking`(후보가 여러 개라 되묻기) 세 가지를 쓴다. 원문: `.../app-integration-guide/use-cases/radio-app`
- 수신 action 이름은 `<패키지명>.<function.name>`이다 (예: `com.offcourse.picko.REQUEST_PICKUP`). 에뮬레이터 실측(2026-11-10): `schema.json`의 `function.name`만 매니페스트에 선언했더니 Gleo의 `startActivity`가 `No Activity found`(-91)로 실패했다. 실행 요청은 `{"packageName", "component", "action", "actionParameters"}`이고 Gleo가 `act=<패키지명>.<FUNCTION>`, `pkg=<패키지명>` intent를 만든다. `function.name`과 같은 action을 선언하라는 문서 요약은 이 점에서 부정확했다.
- 에뮬레이터 검증 명령: `component: activity`는 `adb shell am start -a <패키지>.<FUNCTION> -n <패키지>/<액티비티> --es ACTION_PARAM '<json>'`, `component: service`는 `adb shell am broadcast -a <패키지>.<FUNCTION> -p <패키지> --es ACTION_PARAM '<json>'`. `adb shell`이 따옴표를 먹으므로 JSON의 `"`는 `\"`로 이스케이프한다. 이 방법은 앱 쪽 수신만 검증한다. Gleo가 함수를 고르는 부분은 실제 Gleo가 필요하다.
- 에뮬레이터에서 Gleo 호출(2026-11-10 확인): `adb shell cmd voiceinteraction show`가 호출어 없는 PTT와 같다. 삐 소리(약 1초) 뒤 약 4초 안에 말해야 하고 `RESULT_TIMEOUT`이면 인식 실패다. `KEYCODE_VOICE_ASSIST`는 이 이미지에서 처리되지 않는다. 마이크는 Extended controls의 "Enable Host Microphone Access"를 켜고 macOS 입력 장치를 내장 마이크로 둔다 (블루투스 이어폰이 기본 입력이면 무음이 될 수 있다). Gleo는 부팅 때 `schema.json`을 읽으므로 앱 설치 후 재부팅해야 인식된다.
- 에뮬레이터 연락처(2026-11-11 확인): 에뮬레이터는 AAOS 멀티 유저이고 연락처 프로바이더(`ContactsProvider2`)는 현재 유저(User 10)에만 설치돼 있다. `adb shell`은 기본이 User 0이라 `content`, `pm grant`에 `--user 10`을 붙여야 한다 (`adb shell am get-current-user`로 확인). 더미 연락처는 `content insert --user 10 --uri content://com.android.contacts/raw_contacts`와 `.../data`로 넣는다. 블루투스 연락처(2026-11-11 확인): 휴대폰 AVD(`Pixel_10_Pro`)를 차량 에뮬레이터 옆에 띄우면 에뮬레이터 `netsim` 가상 블루투스로 서로 검색·페어링된다(차량 설정 → 블루투스 → 기기 추가, 휴대폰에서 "연락처와 통화 기록 접근도 허용"을 켜고 Pair). 연결되면 차량의 `PbapClientService`가 휴대폰 연락처를 받아 표준 `ContactsContract` DB에 `account_type=com.android.bluetooth.pbapsink`로 넣고, Picko의 이름 조회가 그 연락처를 찾는다. 번호 표기는 바뀔 수 있다(`010-7777-8888` → `010-777-78888`). 실제 휴대폰과 실차에서의 동작은 확인하지 못했다.
- 에뮬레이터 실제 음성 호출 성공(2026-11-11): 앱 설치 후 재부팅하고 `adb shell cmd voiceinteraction show`로 "엄마 강남역에서 픽업해줘"라고 말하자 STT가 인식하고 Gleo가 `com.offcourse.picko.REQUEST_PICKUP`(`passenger=엄마`, `place=강남역`)을 골라 `sendActionCall`로 `MainActivity`를 실행했고 앱이 `ACTION_PARAM`을 받았다(logcat 태그 `[GAA]`, `ami-sdk`, `AppActionExecution`). 위 "에뮬레이터 상태" 항목의 음성 호출 실패 기록은 이 환경에서는 더 이상 재현되지 않는다. 앱이 콜백(`APP_ACTION_RESPONSE`)을 보내지 않을 때의 Gleo 동작은 로그에서 확인하지 못했다.
- 앱 콜백 타임아웃 실측(2026-11-11): 앱이 `APP_ACTION_RESPONSE`를 보내지 않으면 Gleo는 `sendActionCall` 후 **20초**를 기다리고(문서의 "7초"와 다르다) 서버에 `actionCallback=timeout` 이벤트를 보낸다. 서버는 "앱에서 처리가 조금 늦어지고 있어요. 잠시 기다려도 실행이 안 되면 화면에서 직접 진행해 주세요." (`gleo_app_action_timeout`)를 말한다. 앱이 호출을 정상 수신해도 콜백이 없으면 나온다. Gleo는 재호출하지 않는다. 공식 문서(`action_callback`, 라디오 앱 사례)에는 JSON 형식만 있고 브로드캐스트 action과 extra 키 이름은 없다. Gleo 앱(`ai.umos.ambientai`)에서 확인한 것: action `ai.pleos.playground.intent.action.APP_ACTION_RESPONSE`는 manifest가 아니라 코드에서 동적으로 등록된 수신기가 받고(`processReceiver(): APP_ACTION_RESPONSE`), `adb shell am broadcast --user 10`으로 보내면 수신된다. 앱에서 `Context.sendBroadcast(Intent(<위 action>).putExtra("packageName", <앱 패키지>).putExtra("response", <JSON 문자열>))`로 보내면 일반 앱도 수신된다(2026-11-11 실제 음성 호출로 확인). `response`는 `{"functionName","status","message","mode"}` JSON이고, 이렇게 보내면 20초 타임아웃 문구가 나오지 않는다. 영어 `message`("Found the contact for 엄마.")를 Gleo가 한국어로 바꿔 말한다("엄마 연락처로 강남역 픽업을 요청할게요."). `status=asking`이면 Gleo가 되묻는 문장을 말하고("아빠라는 이름의 연락처가 2개 있어요. 어떤 아빠로 서울역 픽업을 요청할까요?") 곧바로 `ListeningPassive`로 돌아간다. 사용자 답변이 앱으로 다시 오는 경로는 확인하지 못했다 (미검증).
- 문자 전송 검증(2026-11-12, 에뮬레이터 Connect v2.1.0 + 휴대폰 AVD `Pixel_10_Pro`): 블루투스로 페어링하면 차량의 `MapClientService`가 `MceStateMachine state=Connected`가 된다(휴대폰의 "Text Messages" 토글 필요). Pleos Connect SDK 문서 색인에는 문자·전화·연락처 SDK가 없다. 차량의 시스템 메시지 앱 `ai.umos.callandmessage`(`/system_ext/app`)의 `SendMessageOverlayActivity`가 `ACTION_SENDTO`(`smsto:<번호>`)를 받아 오버레이를 띄우지만, 번호만 URI에서 읽고(`getSchemeSpecificPart`) `sms_body`·`EXTRA_TEXT` 처리는 dex 문자열에서 찾지 못했다(디컴파일은 아님). 본문은 음성 받아쓰기(`Recording...`)로만 채워지므로 **URL 같은 정해진 본문을 보낼 수 없다**. 앱이 직접 `BluetoothMapClient.sendMessage`를 부르는 방법은 `BLUETOOTH_PRIVILEGED`(`protectionLevel: signature|privileged`)가 필요해 일반 앱은 받을 수 없다고 판단했다(권한 정의로 판단했고 검증 앱으로 호출해 보지는 않았다). 카카오톡은 블루투스 MAP 경로가 없다. 에뮬레이터 휴대폰에는 통신망이 없어 실제 번호로 가는 문자는 확인하지 못했고, 실제 Pleos 기기와 실제 휴대폰에서의 동작은 확인하지 못했다.
- 콘솔 업로드 절차는 문서에서 확인하지 못했다. 문서는 "개발 진행에 따라 계속 개선된다"고 밝힌다.

## 알려진 공백

- Vehicle의 Gradle 좌표와 상세 API 시그니처는 아직 읽지 않았다. 사용하기 전에 원문을 확인한다.
- 앱이 Gleo의 듣기를 직접 시작하는 방법(2026-11-13 확인): 공식 문서(`app-capability`, `core-concepts`, `examples`, SDK 목록)에 없다. 기기에서 `ACTION_ASSIST`는 Gleo `MainActivity`를 열어 `startAmbientAiService()`만 하고 0.3초 뒤 종료하며 듣지 않았다. 음성 세션 시작(`AiAssistantService`)은 `BIND_VOICE_INTERACTION`이고 Gleo 전용 권한(`APP_ACTION`, `SIRI_VOICE_ASSIST`, `STOP_TTS`)은 모두 `signature` 등급이다. 그래서 Gleo는 사용자가 호출어나 PTT로 부를 때만 듣고, 앱이 직접 듣게 하려면 SpeechToText SDK를 쓴다. Gleo 경로(`REQUEST_PICKUP` 수신)는 그대로 둔다.
