package com.offcourse.picko.presentation.home.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

fun NavGraphBuilder.homeNavGraph(onPickupRequested: (PickupRequestModel) -> Unit) {
    composable<HomeDestination> {
        HomeRoute(
            viewModel = hiltViewModel(),
            onPickupRequested = onPickupRequested,
        )
    }
}
