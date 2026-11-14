# 데이터 계층

Google 권장 아키텍처를 따른다. 단일 `app` 모듈이며 모듈 분리 없이 패키지 경계로만 나눈다. 의존성 방향은 `presentation → (domain) → data`다.

```
com.offcourse.picko
├── core/            designsystem, localstorage, network, navigation, common
├── data/<기능>/      repository(인터페이스), repositoryimpl, model, dto(외부 입출력 JSON), datasource, di
│   └── vehicle/     Pleos SDK 연동
├── domain/<기능>/    model, usecase (필요한 기능에만)
└── presentation/<화면>/   MainActivity, MainScreen(네비게이션 연결점), 각 화면
```

## 파일 위치

| `data/<기능>/` 아래 | 담는 것 | 예시 |
| --- | --- | --- |
| `repository/` | 인터페이스 | `VehicleRepository` |
| `repositoryimpl/` | 구현, 가짜 구현 | `FakeVehicleRepository` |
| `model/` | 이 계층의 모델. presentation이 쓰는 타입 | `VehicleLocation` |
| `dto/` | 외부 JSON 입출력 타입(Gleo 콜백, 원격 API 등)과 `toModel()`/`toDto()` 변환. 외부 입출력이 없는 기능에는 만들지 않는다 | `PickupRequestDto` |
| `datasource/` | 데이터 소스 (필요할 때) | |
| `di/` | Hilt 모듈 | `VehicleRepositoryModule` |

## 계층 규칙

| 규칙 | 내용 |
| --- | --- |
| SDK 경계 | **Pleos SDK import는 `data/vehicle`, `data/speech`, `data/navi`에서만 허용한다.** 다른 패키지는 SDK 타입을 쓰지 않고 Repository 인터페이스로만 접근한다 |
| 타입 변환 | `data/vehicle`, `data/speech`, `data/navi`는 SDK 타입을 `domain`/`presentation`에 노출하지 않고 `XxxModel`로 변환해서 넘긴다. DTO 사용 여부는 SDK 추가 후 정한다 |
| 구현체 참조 | `presentation`은 `repositoryimpl`, `datasource`, `dto`를 참조하지 않는다. 인터페이스와 `model`만 쓴다 |
| `core` | 특정 기능에 의존하지 않는다 |

| Don't | Do |
| --- | --- |
| `presentation`에서 Pleos SDK 타입 import | `VehicleRepository`가 돌려주는 `VehicleLocation` 사용 |
| ViewModel에 `FakeVehicleRepository` 주입 | `VehicleRepository` 인터페이스 주입 |

## Hilt와 가짜 구현

- 기능 패키지마다 `di/`에 모듈을 둔다.
- 테스트와 SDK가 없는 환경에서는 `data/vehicle`의 가짜 구현으로 교체할 수 있어야 한다.
- 실제 SDK 구현이 준비되면 `VehicleRepositoryModule`의 바인딩만 교체한다.

```kotlin
interface VehicleRepository {
    /** 차량 위치를 실시간으로 내보낸다. */
    fun observeLocation(): Flow<VehicleLocation>
}

@Binds
@Singleton
abstract fun bindVehicleRepository(impl: FakeVehicleRepository): VehicleRepository
```

### 기능별 구현 상태

| 기능(`data/`) | 하는 일 | 구현 |
| --- | --- | --- |
| `contact` | 이름으로 연락처 조회 | 실제 (`ContactRepositoryImpl`) |
| `gleo` | Gleo 요청 파싱과 콜백 | 실제 (`GleoRepositoryImpl`) |
| `speech` | 말로 하는 픽업 요청 듣기(STT)와 문장 해석, 안내 문구 읽기(TTS) | 실제 (`SpeechRecognitionRepositoryImpl`·`PleosSpeechTextSource`, `PleosSpeechSynthesisRepository`). STT와 TTS 모두 온디바이스(`Mode.ON_DEVICE`)라 앱 등록과 클라이언트 ID/시크릿이 필요 없다. STT는 에뮬레이터에서 `ON_DEVICE`로 연결되고 한국어(`ko_KR`)로 오디오를 처리하는 것까지 확인했고, 실제 발화 인식 품질은 확인 전이다 |
| `vehicle` | 차량 위치 | 실제 (`FusedLocationVehicleRepository`). 위치를 받지 못하면 이유(권한 거부, 미지원, 받지 못함)를 `VehicleLocationException`으로 전달하고 화면이 알림 대화상자로 안내한다. 값도 오류도 없는 경우가 있어 호출하는 쪽(`PickupFlowViewModel`)이 5초 상한을 두고 "받지 못함"으로 본다. `showLocationAccessBlockedPopup`은 사용자가 대화상자의 `개인정보 설정 확인`을 눌렀을 때만 연다. SDK 가 없는 환경에서는 `FakeVehicleRepository`로 바인딩을 바꾼다 |
| `place` | 장소 검색, 주변 안전 정차 장소 추천 | 가짜 (`FakePlaceRepository`) |
| `pickuplink` | 위치 공유 링크 전송, 탑승자 위치 대기, 취소 알림 | 가짜 (`FakePickupLinkRepository`) |
| `route` | 지도에 그릴 차량·도보 경로와 목적지 경로 | 차량 경로는 실제 (`KakaoRouteRepository`, 카카오모빌리티 자동차 길찾기). 도보와 목적지 경로는 가짜 (`FakeRouteRepository`), 호출이 실패해도 차량 경로를 가짜로 대신한다 |
| `navi` | 차량 내비(NaviHelper)에 소요 시간 묻기(`requestTrip`)와 안내 시작(`requestRoute`) | 실제 (`NaviHelperRepository`, 연결은 `NaviHelperConnection`). 경로 좌표는 받을 수 없어 지도 선은 `route`가 맡는다. 픽업 경로의 차량 소요 시간만 `FindPickupRoutesUseCase`가 내비 값으로 바꾼다. 안내를 시작한 뒤에는 내비가 맡고 앱은 흐름을 끝낸다 |

## 로컬 저장 (`core/localstorage`)

DataStore(Preferences)를 쓴다. 저장소 하나는 `@AppDataStore` 한정자로 구분하고, 저장하는 값마다 인터페이스와 구현을 둔다.

| 구성 | 규칙 |
| --- | --- |
| 인터페이스 | `XxxStorage`(`OnboardingStorage`). 읽기는 `Flow`, 쓰기는 `suspend` |
| 구현 | `XxxStorageImpl`. 키는 파일 안의 `private val`로 두고, 읽기 실패(`IOException`)는 기본값으로 처리한다 |
| Hilt | `LocalStorageModule`이 `@Binds`로 인터페이스를 연결하고 `@Provides`로 DataStore를 만든다 |

UseCase 조건: [domain-layer](domain-layer.md)

## 시연 차량 (디버그 전용)

에뮬레이터의 내비는 위치를 받지 않아 이동에 따른 시간 변화를 볼 수 없다([pleos-sdk-index](../references/pleos-sdk-index.md)). 시연할 때는 `demo.drive`를 켠다. `local.properties`에 `demo.drive=true`를 쓰거나 `./gradlew installDebug -Pdemo.drive=true`로 설치한다. `BuildConfig.DEBUG`와 `DEMO_DRIVE`가 모두 참일 때만 동작해 릴리스에서는 켜지지 않는다.

- `DemoVehicleRepository`(`data/vehicle`): 서울역에서 대치동 학원가 사거리까지 카카오모빌리티 자동차 경로의 좌표를 따라 40m/s로 1초마다 위치를 내보낸다(길찾기를 쓰지 못하면 가짜 직선 경로). `내비게이션 안내 시작`을 누르면(`VehicleRepository.onNavigationStarted`) 출발하고 끝에서 멈추며(그 전에는 출발지에 서 있다), 앱을 다시 시작하면 처음부터 간다. 지도에 그린 경로선과 같은 좌표라 마커가 선 위를 간다.
- `DemoNaviRepository`(`data/navi`): 시연 차량의 현재 위치에서 목적지까지 직선거리를 같은 속도로 나눠 `previewTrip`의 시간과 거리를 돌려준다. `startGuidance`는 실제 내비에 맡긴다.
- 바인딩은 `VehicleRepositoryModule`, `NaviRepositoryModule`이 조건으로 고른다.

