package com.offcourse.picko.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.home.navigation.HomeDestination
import com.offcourse.picko.presentation.home.navigation.homeNavGraph
import com.offcourse.picko.presentation.pickup.navigation.PickupFlowDestination
import com.offcourse.picko.presentation.pickup.navigation.navigateToPickupFlow
import com.offcourse.picko.presentation.pickup.navigation.pickupNavGraph
import com.offcourse.picko.presentation.splash.navigation.SplashDestination
import com.offcourse.picko.presentation.splash.navigation.splashNavGraph
import kotlinx.coroutines.flow.Flow

/**
 * 앱 화면의 진입점. [MainViewModel]의 부수 효과를 [MainScreen]에 연결한다.
 * Activity 의 `by viewModels()`와 같은 Activity 저장소에서 같은 인스턴스를 얻는다.
 * NavHost 의 화면 안에서 호출하면 저장소 주인이 달라져 다른 인스턴스가 되므로 Activity 의 `setContent`에서 부른다.
 */
@Composable
fun MainApp() {
    val viewModel: MainViewModel = hiltViewModel()

    MainScreen(
        sideEffect = viewModel.sideEffect
    )
}

/** 네비게이션 연결점. 스플래시, 홈, 픽업 흐름을 하나의 NavHost 로 잇는다. */
@Composable
private fun MainScreen(sideEffect: Flow<MainSideEffect>) {
    val navController = rememberNavController()

    LaunchedEffect(navController, sideEffect) {
        sideEffect.collect { effect ->
            when (effect) {
                is MainSideEffect.StartPickup -> navController.startPickup(effect.request, isGleoRequest = true)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = SplashDestination,
        modifier = Modifier
            .fillMaxSize()
            .background(PickoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        splashNavGraph(
            onFinished = {
                navController.navigate(HomeDestination) {
                    popUpTo<SplashDestination> { inclusive = true }
                }
            },
        )

        homeNavGraph(onPickupRequested = { request -> navController.startPickup(request, isGleoRequest = false) })

        pickupNavGraph(
            navController = navController,
            // 처음 화면(홈)으로 돌아간다. 흐름 그래프가 pop 되면서 흐름의 ViewModel 도 함께 사라진다.
            onFlowFinished = { navController.popBackStack<PickupFlowDestination>(inclusive = true) },
            onPickupRequested = { request -> navController.startPickup(request, isGleoRequest = false) },
        )
    }
}

/**
 * 새 픽업 흐름을 시작한다. 진행 중이던 흐름은 버린다.
 * 스플래시 중에 호출되면 스플래시를 홈으로 바꾼 뒤 그 위에 흐름을 올려, 뒤로 가면 홈으로 돌아가게 한다.
 */
private fun NavController.startPickup(request: PickupRequestModel, isGleoRequest: Boolean) {
    if (currentDestination?.hasRoute<SplashDestination>() == true) {
        navigate(HomeDestination) {
            popUpTo<SplashDestination> { inclusive = true }
        }
    }

    navigateToPickupFlow(request, isGleoRequest) {
        popUpTo<PickupFlowDestination> { inclusive = true }
    }
}
