# 아키텍처

Google 권장 아키텍처를 따른다. 단일 `app` 모듈이며 모듈 분리 없이 패키지 경계로만 나눈다. 의존성 방향은 `presentation → (domain) → data`다.

```
com.offcourse.pickitup
├── core/            designsystem, localstorage, network, navigation, common
├── data/<기능>/      repository(인터페이스), repositoryimpl, model(DTO), datasource, di
│   └── vehicle/     Pleos SDK 연동
├── domain/<기능>/    model, usecase (필요한 기능에만)
└── presentation/<화면>/   MainActivity, MainScreen(네비게이션 연결점), 각 화면
```

## 계층 규칙

- **Pleos SDK import는 `data/vehicle`에서만 허용한다.** 다른 패키지는 SDK 타입을 직접 쓰지 않고 repository 인터페이스를 통해서만 접근한다.
- `domain`은 선택적이다. UseCase는 둘 이상의 repository를 조합하거나 같은 로직이 두 화면 이상에서 쓰일 때만 만든다. 단순 조회는 ViewModel이 repository를 바로 쓴다.
- `presentation`은 `data`의 구현체(`repositoryimpl`, `datasource`)를 참조하지 않는다. 인터페이스만 쓴다.
- `core`는 특정 기능에 의존하지 않는다.

## 상태 관리

MVVM의 단일 소스(ViewModel)와 MVI의 단방향 흐름을 합친 방식이다. Intent 클래스는 강제하지 않는다. 구현 예시는 OfficeMeong의 `presentation/favorite`을 참조한다.

- `XxxContract.kt`에는 `XxxState`(`@Immutable data class`)와 `XxxSideEffect`(sealed interface)만 둔다.
- ViewModel은 `MutableStateFlow`를 private으로 두고 `StateFlow<XxxState>`만 노출한다. 일회성 이벤트(스낵바, 화면 이동)는 `Channel`을 `receiveAsFlow()`로 노출한다.
- 사용자 동작은 ViewModel의 public 함수로 받는다. 이름은 동작으로 짓는다 (`onSpotSelected`). state 변경은 ViewModel 안에서만 한다.
- 화면은 `XxxRoute`(ViewModel 보유, state·sideEffect 수집, 네비게이션 람다 연결)와 `private XxxScreen`(state와 콜백만 받는 순수 UI)으로 나눈다. Screen에 ViewModel을 넘기지 않는다.
- 비동기 데이터 필드는 `core/common`의 `UiState<T>`(Loading/Success/Error) 래퍼를 쓴다. 화면 상태 클래스 이름은 `XxxState`이며 `UiState`와 구분한다.
- 실시간 스트림(차량 위치, 탑승자 위치)은 ViewModel에서 `combine` 등으로 하나의 state로 합친다. Route와 Screen이 Flow를 직접 구독하지 않는다.
- 비동기는 Coroutines와 Flow를 쓴다.

## DI

- Hilt. 기능 패키지마다 `di/`에 모듈을 둔다.
- 테스트와 SDK가 없는 환경에서는 `data/vehicle`의 가짜 구현으로 교체할 수 있어야 한다.

## 네이밍

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 화면 | `XxxRoute`, `XxxScreen`, `XxxViewModel`, `XxxContract.kt`(`XxxState`, `XxxSideEffect`) | `PickupRoute` |
| 하위 컴포넌트 | `presentation/<화면>/component/` | `SpotCard` |
| UI 표시용 모델 | `XxxUiModel` (`presentation/<화면>/model/`) | `SpotUiModel` |
| 네비게이션 | `presentation/<화면>/navigation/XxxNavigation.kt` | |
| Repository | 인터페이스 `XxxRepository`(`repository/`), 구현 `XxxRepositoryImpl`(`repositoryimpl/`) | `VehicleRepository` |
| 데이터 모델 | data 계층 모델 `XxxModel`, 원격 DTO `XxxDto` | |
| UseCase | `XxxUseCase`, 동사로 시작 | `RecommendSpotUseCase` |
| Hilt 모듈 | `XxxRepositoryModule` (`di/`) | |
| 패키지 | 소문자 한 단어, 언더스코어 없음 | `repositoryimpl` |

- `data/vehicle`은 Pleos SDK 타입을 `domain`/`presentation`에 노출하지 않고 `XxxModel`로 변환해서 넘긴다. DTO 사용 여부는 SDK 추가 후 정한다.

## 코드 컨벤션

- Kotlin 공식 코드 스타일(`kotlin.code.style=official`)을 따른다. 별도 포매터·린터 도구는 프로토타입 이후에 검토한다.
- 식별자(클래스, 함수, 변수)는 영어로 쓴다.
- 주석과 KDoc은 한국어로 쓴다. 코드만 봐도 아는 설명은 쓰지 않고, 이유가 필요한 곳에만 쓴다.
- 사용자에게 보이는 문자열(음성 안내 문구 포함)은 코드에 직접 쓰지 않고 `strings.xml`에 둔다.
- 매직 넘버(터치 영역, 거리, 시간 임계값)는 이름 있는 상수로 둔다.

### 코드 가독성

이름과 구조만으로 책임과 데이터 흐름이 보이게 쓴다. 아래는 자동 검사가 없으므로 변경 후 직접 점검한다.

- 파라미터가 둘 이상인 선언과 호출은 각 파라미터를 별도 줄에 쓰고 마지막 쉼표를 둔다. 중첩 호출도 들여쓰기와 줄바꿈으로 구조를 드러낸다.
- Modifier 체인은 연산자마다 한 줄씩 쓴다. 한 줄에 두 개 이상 이어 붙이지 않는다.

  ```kotlin
  Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .clickable(onClick = onClick)
  ```

- Route, Screen, 컴포저블에서는 인접한 UI 요소와 논리적으로 구분되는 블록 사이에 빈 줄을 둔다. 상태 선언, 이벤트 처리, 화면 구성도 읽기 좋은 단위로 구분한다.
- 여러 줄 람다는 본문을 들여쓰고, 상태 변경·조건 처리·UI 요소 사이의 경계를 빈 줄로 나눈다. 한 줄 람다는 간단한 동작에만 쓴다.
- 요소 간격과 위치는 패딩, 정렬, 레이아웃 구성으로 표현한다. 고정 너비·높이는 콘텐츠 크기나 weight로 안 될 때만 쓴다.
- Screen은 흐름을 한눈에 읽을 수 있게 유지한다. 독립된 역할이 있는 UI는 결합도와 응집도를 판단해 `component/`로 분리하되, 파일 수를 늘리기 위한 분리는 하지 않는다.
- 상태는 실제로 쓰는 가장 작은 컴포저블에서 읽는다. 자주 바뀌는 값(스크롤, 실시간 위치)은 필요한 곳까지 읽기를 미룬다.

## 미확정

- 네비게이션 방식 (Navigation Compose 등)
- 서체, 디자인 토큰, Figma 수치 변환 규칙 (디자인 시안 확정 후)
