# 네비게이션

| 항목 | 규칙 |
| --- | --- |
| 라이브러리 | Navigation 2(`navigation-compose`)의 타입 안전 라우트. Navigation 3는 쓰지 않는다 |
| 라우트 | `@Serializable` `data object` / `data class`로 선언한다. 이름은 `XxxDestination`이라서 화면 컴포저블 `XxxRoute`와 구분한다 |
| 위치 | `presentation/<화면>/navigation/XxxNavigation.kt`에 라우트, `NavController.navigateToXxx()`, `NavGraphBuilder.xxxNavGraph()`를 둔다. 한 흐름의 여러 화면은 흐름의 `XxxNavigation.kt` 하나에 함께 둔다 |
| 연결 | `MainScreen`의 `NavHost`에서 각 화면과 흐름의 NavGraph를 연결한다 |
| 이동 | Route가 받은 람다로만 이동한다. Screen과 ViewModel은 `NavController`를 알지 못한다 |
| 흐름 그래프 | 여러 화면이 상태를 공유하면 `navigation<XxxFlowDestination>`으로 묶는다 ([presentation](presentation.md#흐름-범위-viewmodel)) |
| 시작 | `SplashDestination`에서 시작해 홈으로 넘어가면서 스플래시를 지운다. Gleo 호출이 스플래시 중에 오면 스플래시를 홈으로 바꾼 뒤 그 위에 흐름을 올려, 뒤로 가면 홈으로 돌아간다 |
| 현재 위치 확인 | `NavDestination.hasRoute<T>()`는 `androidx.navigation.NavDestination.Companion.hasRoute`를 import한다 |

| Don't | Do |
| --- | --- |
| Screen에 `NavController` 전달 | Route에 이동 람다 전달 |
| ViewModel이 화면 이동을 직접 호출 | `XxxSideEffect`로 보내고 Route가 람다 호출 ([presentation](presentation.md)) |
| 흐름 도중 새 요청을 같은 그래프 위에 쌓음 | `popUpTo<XxxFlowDestination> { inclusive = true }`로 이전 흐름을 지우고 시작 |

Back 하드 키: 최상위 화면(홈)은 앱을 종료하지 않고 `moveTaskToBack(true)`로 숨긴다 ([PLEOS](../PLEOS.md#back-하드-키-문서상-필수)). 내비게이션(주행) 화면에서는 취소 확인을 먼저 띄운다.
