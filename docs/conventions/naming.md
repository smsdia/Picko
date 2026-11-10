# 네이밍

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 화면 | `XxxRoute`, `XxxScreen`, `XxxViewModel` | `PickupRoute` |
| Contract | `XxxContract.kt`에 `XxxState`, `XxxSideEffect`, (선택) `XxxAction` | `PickupState` |
| UI 전용 상태 | `XxxScreenState`, `rememberXxxScreenState()` | `MapScreenState` |
| 비동기 래퍼 | `core/common`의 `UiState<T>` (화면 상태 `XxxState`와 구분) | |
| 하위 컴포넌트 | `presentation/<화면>/component/` | `SpotCard` |
| UI 표시용 모델 | `XxxUiModel` (`presentation/<화면>/model/`) | `SpotUiModel` |
| 네비게이션 | `presentation/<화면>/navigation/XxxNavigation.kt` | |
| Repository | 인터페이스 `XxxRepository`(`repository/`), 구현 `XxxRepositoryImpl`(`repositoryimpl/`) | `VehicleRepository` |
| 데이터 모델 | data 계층 모델 `XxxModel`, 원격 DTO `XxxDto` | |
| UseCase | `XxxUseCase`, 동사로 시작 | `RecommendSpotUseCase` |
| Hilt 모듈 | `XxxRepositoryModule` (`di/`) | |
| 패키지 | 소문자 한 단어, 언더스코어 없음 | `repositoryimpl` |
| 문자열 리소스 | [resources](resources.md) | |

| Don't | Do |
| --- | --- |
| `UiStateHolder` (UiState와 혼동) | `XxxScreenState` |
| `repository_impl` 패키지 | `repositoryimpl` |
