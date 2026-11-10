# 네비게이션

| 항목 | 규칙 |
| --- | --- |
| 라이브러리 | Navigation 2(`navigation-compose`)의 타입 안전 라우트. Navigation 3는 쓰지 않는다 |
| 라우트 | `@Serializable` `data object` / `data class`로 선언한다 |
| 위치 | `presentation/<화면>/navigation/XxxNavigation.kt`에 라우트, `NavController.navigateToXxx()`, `NavGraphBuilder.xxxNavGraph()`를 둔다 |
| 연결 | `MainScreen`의 `NavHost`에서 각 화면의 NavGraph를 연결한다 |
| 이동 | Route가 받은 람다로만 이동한다. Screen과 ViewModel은 `NavController`를 알지 못한다 |

| Don't | Do |
| --- | --- |
| Screen에 `NavController` 전달 | Route에 이동 람다 전달 |
| ViewModel이 화면 이동을 직접 호출 | `XxxSideEffect`로 보내고 Route가 람다 호출 ([presentation](presentation.md)) |

Back 하드 키: 최상위 화면은 앱을 종료하지 않고 숨긴다 ([PLEOS](../PLEOS.md#back-하드-키-문서상-필수)).
