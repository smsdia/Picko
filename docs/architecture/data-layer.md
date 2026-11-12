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
| SDK 경계 | **Pleos SDK import는 `data/vehicle`에서만 허용한다.** 다른 패키지는 SDK 타입을 쓰지 않고 Repository 인터페이스로만 접근한다 |
| 타입 변환 | `data/vehicle`은 SDK 타입을 `domain`/`presentation`에 노출하지 않고 `XxxModel`로 변환해서 넘긴다. DTO 사용 여부는 SDK 추가 후 정한다 |
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
| `vehicle` | 차량 위치 | 가짜 (`FakeVehicleRepository`). Pleos SDK 구현이 준비되면 교체 |
| `place` | 장소 검색, 주변 안전 정차 장소 추천 | 가짜 (`FakePlaceRepository`) |
| `pickuplink` | 위치 공유 링크 전송, 탑승자 위치 대기, 취소 알림 | 가짜 (`FakePickupLinkRepository`) |
| `route` | 차량·도보 경로와 목적지 경로 | 가짜 (`FakeRouteRepository`) |

## 로컬 저장 (`core/localstorage`)

DataStore(Preferences)를 쓴다. 저장소 하나는 `@AppDataStore` 한정자로 구분하고, 저장하는 값마다 인터페이스와 구현을 둔다.

| 구성 | 규칙 |
| --- | --- |
| 인터페이스 | `XxxStorage`(`OnboardingStorage`). 읽기는 `Flow`, 쓰기는 `suspend` |
| 구현 | `XxxStorageImpl`. 키는 파일 안의 `private val`로 두고, 읽기 실패(`IOException`)는 기본값으로 처리한다 |
| Hilt | `LocalStorageModule`이 `@Binds`로 인터페이스를 연결하고 `@Provides`로 DataStore를 만든다 |

UseCase 조건: [domain-layer](domain-layer.md)
