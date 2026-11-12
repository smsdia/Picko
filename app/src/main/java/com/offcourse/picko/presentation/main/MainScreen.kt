package com.offcourse.picko.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
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

/** 네비게이션 연결점. 스플래시, 홈, 픽업 흐름을 하나의 NavHost 로 잇는다. */
@Composable
fun MainScreen(sideEffect: Flow<MainSideEffect>) {
    val navController = rememberNavController()

    LaunchedEffect(navController, sideEffect) {
        sideEffect.collect { effect ->
            when (effect) {
                is MainSideEffect.StartPickup -> navController.startPickup(effect.request)
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

        homeNavGraph()

        pickupNavGraph(
            navController = navController,
            // 처음 화면(홈)으로 돌아간다. 흐름 그래프가 pop 되면서 흐름의 ViewModel 도 함께 사라진다.
            onFlowFinished = { navController.popBackStack<PickupFlowDestination>(inclusive = true) },
        )
    }
}

/**
 * 새 픽업 흐름을 시작한다. 진행 중이던 흐름은 버린다.
 * 스플래시 중에 호출되면 스플래시를 홈으로 바꾼 뒤 그 위에 흐름을 올려, 뒤로 가면 홈으로 돌아가게 한다.
 */
private fun NavController.startPickup(request: PickupRequestModel) {
    if (currentDestination?.hasRoute<SplashDestination>() == true) {
        navigate(HomeDestination) {
            popUpTo<SplashDestination> { inclusive = true }
        }
    }

    navigateToPickupFlow(request) {
        popUpTo<PickupFlowDestination> { inclusive = true }
    }
}
