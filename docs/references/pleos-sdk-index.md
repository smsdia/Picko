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
- 콘솔 업로드 절차는 문서에서 확인하지 못했다. 문서는 "개발 진행에 따라 계속 개선된다"고 밝힌다.

## 알려진 공백

- TextToSpeech, SpeechToText, NaviHelper, Vehicle의 Gradle 좌표와 상세 API 시그니처는 아직 읽지 않았다. 사용하기 전에 원문을 확인한다.
