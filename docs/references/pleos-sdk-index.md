# Pleos SDK 참조 색인

> 출처: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/ (확인일 2026-11-08)
> 이 파일은 원문 복사가 아니라 **요약**이다. 문서 저작권은 Hyundai Motor Group에 있다. 구현 전에 해당 원문 페이지를 열어 시그니처를 확인한다. 요약에 없는 API는 존재한다고 가정하지 않는다.

## SDK 목록 (Picko 관련도)

| SDK | 버전(확인 시점) | 용도 | 관련도 | 원문 |
| --- | --- | --- | --- | --- |
| FusedLocation | v1.1.3 | 차량 위치 | 핵심 | `/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro` |
| NaviHelper | v3.0.5 | 목적지·경로·주행 상태 | 핵심 | `/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro` |
| SpeechToText | 미확인 | 음성 인식 | 핵심 | `/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro` |
| TextToSpeech | 미확인 | 음성 합성 | 핵심 | `/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro` |
| Vehicle | SDK v2.3.2 / Service v2.3.21 | 차량 정보 조회·제어 | 차량 정보 안내용 | `/docs/pleos-only/connect-sdk-pleos/Vehicle/intro` |
| Gleo Interface | - | Gleo AI 음성 명령 연동(App Schema) | 핵심 | `/docs/pleos-only/app-integration-guide/guide/core-concepts` |

## FusedLocation

- 권한: `pleos.car.permission.FUSED_LOCATION`
- 진입점 `FusedLocation`: `isSupported()`, `initialize()`, `getLastKnownLocation()`, `registerFusedLocationCallback()`, `unregisterFusedLocationCallback()`, `addProximityAlert()`, `removeProximityAlert()`, `showLocationAccessBlockedPopup()`, `release()`
- 모델 `LocationData`: 위도, 경도, 고도, 방위(bearing), 정확도, 타임스탬프
- 상세: `.../FusedLocation/project-setup`, `.../entry`, `.../declaring-permissions`

## NaviHelper

- 진입점 `NaviHelper`. 사용 순서: 생성 → `initialize()` → `addListener()` → 경로·정보 API 호출 → 콜백 처리 → `removeListener()`, `release()`
- 제공: 목적지 설정·확정·취소, 경로 옵션 변경·재탐색, 경유지 추가·조회·삭제, 경유지 포함 경로 요청, 경로 탐색·주행 상태 이벤트, 안내 정보 조회
- 이벤트: `NaviHelperEventListener`
- 상세: `.../NaviHelper/importing-sdk`, `.../entry`, `.../error-handling`, `.../declaring-permissions`

## SpeechToText

- 진입점 `ai.pleos.playground.stt.SpeechToText`, 생성 예: `SpeechToText(applicationContext, Mode.HYBRID)`
- 메서드: `initialize()`, `addListener()`, `setLocale()`, `request()`, `stop()`, `removeListener()`, `release()`
- `Mode`: HYBRID, SERVER, ON_DEVICE. 결과는 `ResultListener`
- 음성을 텍스트로만 바꾼다. 인식한 명령으로 앱 기능을 실행하려면 Gleo Interface를 쓴다.

## Vehicle

- 차량 통신은 IPC라 capability·get·set 결과가 비동기(`onComplete`, `onFailed`)로 전달된다.
- 진입점 `Vehicle`: `initialize()`, `release()`. 도메인 객체는 `getDoor()`, `getBrake()` 등으로 얻는다. API 종류: `checkXXXCapabilities`, Get, Set, Callback
- 20개 이상 도메인(CarInfo, Door, Tire, Window, DrivingMode, Odometer, EvBattery, HVAC, Seat, Brake, Warning, Light, TurnSignal 등). 속도·기어 도메인은 요약에서 확인되지 않았다.
- 도메인별 권한: `.../Vehicle/vehicle-permission`. 기능 지원 확인: `.../Vehicle/check-capability`
- 샘플 앱: `/docs/pleos-only/tutorial/vehicle-sdk-sample-app`

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
- 리스너: `FusedLocationListener`(`onChanged(location: LocationData?)`, `onError(exception)`), `ProximityAlertListener`(`onRegistered(alertId)`, `onTransition(event)`, `onError(exception)`). 패키지: `ai.pleos.playground.location.fused.*`
- 근접 알림: `addProximityAlert(ProximityAlertRequest, listener)`, `removeProximityAlert(alertId)`, 이벤트 `ProximityAlertEvent`(ENTER/EXIT)
- 에뮬레이터 테스트: 앱 실행 후 `adb emu geo fix <경도> <위도>`(경도가 먼저). 목 위치는 `adb shell appops set --user current <패키지> android:mock_location allow` 후 `LocationManager` 테스트 프로바이더로 주입한다.
- Vehicle SDK는 시작 가이드에서 Pleos Maven 저장소로 가져오며(문서상 2.3.1), 에뮬레이터는 Android SDK API 34, 프로젝트 minSdk 28 이상, RAM 4GB, Cold boot가 필요하다.
- 서버 연결이 필요한 SDK(TextToSpeech, SpeechToText)는 콘솔의 클라이언트 ID/시크릿이 필요하다.

## Gleo Interface 상세

- Gleo AI가 부팅할 때 `AssetManager`로 모든 앱의 `assets/schema.json`을 읽어 DB에 넣는다. 갱신은 앱 설치·삭제 시(ADB 또는 앱 마켓)다. Gleo 팀이 가이드라인 준수를 검토한 뒤 연동이 활성화된다.
- `schema.json` 필드: `packageName`, `appDescription`, `version`, `actionCalls`(**최대 3개**)
- 함수 정의: `type`는 `"function"` 고정, `function.name`은 UPPER_SNAKE_CASE(2~3단어), `description`은 영어 한 문장, `component`는 `"activity"`(전면 UI) 또는 `"service"`(백그라운드), `parameters`는 JSON Schema 객체. 파라미터 타입은 string, number, boolean, array 중 하나이고 enum은 문자열만 가능하다. `required`에는 꼭 필요한 것만 둔다.
- 수신: 함수 이름과 같은 intent action을 `AndroidManifest.xml`의 `intent-filter`에 선언한다 (category `DEFAULT`).
- Callback 응답 구조, 에뮬레이터 테스트 방법, 콘솔 업로드 절차는 문서에서 확인하지 못했다. 문서는 "개발 진행에 따라 계속 개선된다"고 밝힌다.

## 알려진 공백

- TextToSpeech, SpeechToText, NaviHelper, Vehicle의 Gradle 좌표와 상세 API 시그니처는 아직 읽지 않았다. 사용하기 전에 원문을 확인한다.
