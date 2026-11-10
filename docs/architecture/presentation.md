# 화면 계층

MVVM의 단일 소스(ViewModel)와 MVI의 단방향 흐름을 합친 방식이다. 화면 이동은 [navigation](navigation.md), 이름은 [naming](../conventions/naming.md)을 따른다.

## Route와 Screen

| 구성 | 가시성 | 책임 |
| --- | --- | --- |
| `XxxRoute` | `internal` | `hiltViewModel()`로 ViewModel을 얻고, State와 SideEffect를 수집하고, 네비게이션 람다를 연결한다. 어디로 이동할지는 `XxxNavigation.kt`가 정한다 |
| `XxxScreen` | `private` | `state`와 람다만 받는 순수 UI. ViewModel을 받지 않아서 Preview와 테스트에 ViewModel이 필요 없다 |

- 모든 `XxxScreen`에 `@Preview`를 두고 `PickoTheme`로 감싼다. 하위 `component/`의 Preview는 필수가 아니다.
- Screen은 이벤트 람다를 4개 이상 받으면 `onAction: (XxxAction) -> Unit` 하나로 묶는 것을 권장한다. 같은 종류의 입력이 늘어 읽기 어려울 때도 묶을 수 있다. 3개 이하는 이벤트마다 람다 하나씩 받는다.

| Don't | Do |
| --- | --- |
| `private fun PickupScreen(viewModel: PickupViewModel)` | `private fun PickupScreen(state: PickupState, onRetry: () -> Unit)` |
| Screen이나 ViewModel이 `NavController`를 앎 | Route가 받은 람다로만 이동 |
| Route와 Screen이 Flow를 직접 구독 | ViewModel이 `combine` 등으로 하나의 State로 합침 |

## Contract 파일

화면마다 `XxxContract.kt` 하나에 최상위 타입으로 둔다. `object`로 감싸지 않는다. 파일이 길어지면 파일만 나누고 타입 이름은 유지한다.

| 타입 | 종류 | 둘 때 |
| --- | --- | --- |
| `XxxState` | `@Immutable data class` | 항상 |
| `XxxSideEffect` | `sealed interface` | 일회성 이벤트가 있을 때 |
| `XxxAction` | `sealed interface` | `onAction`으로 묶을 때 |

| Don't | Do |
| --- | --- |
| `object PickupContract { data class State(...) }` | `PickupContract.kt`에 `data class PickupState`, `sealed interface PickupSideEffect`를 최상위로 |

## State

- ViewModel은 `MutableStateFlow`를 private으로 두고 `StateFlow<XxxState>` 하나만 노출한다.
- State 변경은 ViewModel 안에서만 한다.
- 다이얼로그 표시 여부는 State에 `isXxxDialogVisible`로 둔다.
- 비동기 데이터 필드는 `core/common`의 `UiState<T>`(Loading/Success/Error)를 쓴다. 화면 전체 상태 `XxxState`와 구분한다.
- State의 리스트는 `ImmutableList`를 쓴다 (`kotlinx-immutable`, 의존성에 포함됨).
- 설정 변경이나 프로세스 종료에서 살아남아야 하는 비즈니스 상태는 `ScreenState`가 아니라 State에 둔다.

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val throwable: Throwable) : UiState<Nothing>
}
```

## 사용자 동작

- 사용자 동작은 ViewModel의 public 함수로 받는다. 이름은 동작으로 짓는다 (`onSpotSelected`).
- `onAction`을 쓰는 화면은 `onAction(action: XxxAction)` 하나로 받고 `when`으로 분기한다.

## SideEffect

- 일회성 이벤트(토스트, 스낵바, 화면 이동)는 `XxxSideEffect`로 보낸다. State에 두지 않는다.
- ViewModel에서 `Channel(BUFFERED)`를 `receiveAsFlow()`로 노출한다. 수집이 멈춘 동안 보낸 이벤트는 돌아오면 한 번 전달된다.
- Route는 화면이 `STARTED`일 때만 수집한다.

| Don't | Do |
| --- | --- |
| `MutableSharedFlow(replay = 0)`로 SideEffect 전달 | `Channel(BUFFERED).receiveAsFlow()` |
| 토스트나 이동 상태를 State에 둠 | `XxxSideEffect` |
| 다이얼로그 표시를 SideEffect로 전달 | State의 `isXxxDialogVisible` |

## ScreenState (UI 전용 상태)

| 상황 | 방법 |
| --- | --- |
| 변수 한두 개, 로직 없음 | Route나 Screen에서 `remember` / `rememberSaveable` |
| UI 전용 변수 3개 이상이 묶이거나, 스크롤·포커스·`derivedStateOf` 때문에 읽기 어려움 | `XxxScreenState`와 `rememberXxxScreenState()` |

`UiStateHolder`라는 이름은 `UiState`와 혼동되므로 쓰지 않는다.

## ViewModel

- Repository 인터페이스와 UseCase만 쓴다. `repositoryimpl`, `datasource`, Pleos SDK를 직접 쓰지 않는다. ([data-layer](data-layer.md))
- 화면에 보이는 텍스트를 ViewModel에서 만들지 않는다. State에는 값(`count`)이나 에러 타입을 두고 UI에서 `stringResource`로 만든다. ([resources](../conventions/resources.md))
- 실시간 스트림(차량 위치, 탑승자 위치)은 `combine` 등으로 하나의 State로 합친다.
- 비동기는 Coroutines와 Flow를 쓴다.
