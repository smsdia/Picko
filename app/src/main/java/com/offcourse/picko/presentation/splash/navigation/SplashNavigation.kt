package com.offcourse.picko.presentation.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.offcourse.picko.presentation.splash.SplashRoute
import kotlinx.serialization.Serializable

@Serializable
data object SplashDestination

fun NavGraphBuilder.splashNavGraph(onFinished: () -> Unit) {
    composable<SplashDestination> {
        SplashRoute(onFinished = onFinished)
    }
}
