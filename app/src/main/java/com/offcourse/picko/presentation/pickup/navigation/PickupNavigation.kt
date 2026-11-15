package com.offcourse.picko.presentation.pickup.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.complete.CompleteRoute
import com.offcourse.picko.presentation.pickup.contact.ContactRoute
import com.offcourse.picko.presentation.pickup.driving.DrivingRoute
import com.offcourse.picko.presentation.pickup.link.LinkRoute
import com.offcourse.picko.presentation.pickup.nearby.NearbyRoute
import com.offcourse.picko.presentation.pickup.resolve.ResolveRoute
import com.offcourse.picko.presentation.pickup.spot.SpotRoute
import kotlinx.serialization.Serializable

/**
 * 픽업 흐름 전체를 감싸는 그래프. 인자는 Gleo 요청이고, [PickupFlowViewModel]이 이 그래프 항목에 묶인다.
 * 같은 인자로 다시 이동해도 새 항목이 만들어지도록 호출하는 쪽에서 이전 흐름을 pop한다.
 */
@Serializable
data class PickupFlowDestination(
    val passenger: String,
    // null 이면 장소를 말하지 않은 것. 서버 추천에 목적지가 필요해 다시 말해 달라고 요청한다.
    val place: String? = null,
    // Gleo 호출로 시작했으면 true. 앱이 직접 들은 요청이면 false 라서 Gleo 에 콜백을 보내지 않는다.
    val isGleoRequest: Boolean = true,
)

// 흐름 안의 단계. 같은 그래프 항목의 ViewModel을 공유한다.
@Serializable
data object ResolveDestination

@Serializable
data object ContactDestination

@Serializable
data object LinkDestination

@Serializable
data object NearbyDestination

@Serializable
data object SpotDestination

@Serializable
data object DrivingDestination

@Serializable
data object CompleteDestination

fun NavController.navigateToPickupFlow(
    request: PickupRequestModel,
    isGleoRequest: Boolean,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigate(
        PickupFlowDestination(passenger = request.passenger, place = request.place, isGleoRequest = isGleoRequest),
        builder,
    )
}

/** 연락처 조회 중인 시작 화면은 뒤로 가기로 돌아오지 않게 지운다. */
private fun NavController.navigateToStep(step: PickupStep) {
    val destination: Any = when (step) {
        PickupStep.Contact -> ContactDestination
        PickupStep.Link -> LinkDestination
        PickupStep.Nearby -> NearbyDestination
        PickupStep.Spot -> SpotDestination
        PickupStep.Driving -> DrivingDestination
        PickupStep.Complete -> CompleteDestination
    }

    navigate(destination) {
        popUpTo<ResolveDestination> { inclusive = true }
    }
}

@Composable
private fun NavController.pickupFlowViewModel(entry: NavBackStackEntry): PickupFlowViewModel {
    val flowEntry = remember(entry) { getBackStackEntry<PickupFlowDestination>() }

    return hiltViewModel(flowEntry)
}

fun NavGraphBuilder.pickupNavGraph(
    navController: NavController,
    onFlowFinished: () -> Unit,
    onPickupRequested: (PickupRequestModel) -> Unit,
) {
    navigation<PickupFlowDestination>(startDestination = ResolveDestination) {
        composable<ResolveDestination> { entry ->
            ResolveRoute(
                viewModel = navController.pickupFlowViewModel(entry),
                onNavigate = navController::navigateToStep,
                onFlowFinished = onFlowFinished,
                onPickupRequested = onPickupRequested,
            )
        }

        composable<ContactDestination> { entry ->
            ContactRoute(
                viewModel = navController.pickupFlowViewModel(entry),
                onNavigate = navController::navigateToStep,
                onFlowFinished = onFlowFinished,
                onPickupRequested = onPickupRequested,
            )
        }

        composable<LinkDestination> { entry ->
            LinkRoute(
                viewModel = navController.pickupFlowViewModel(entry),
                onNavigate = navController::navigateToStep,
                onFlowFinished = onFlowFinished,
                onPickupRequested = onPickupRequested,
            )
        }

        composable<NearbyDestination> { entry ->
            NearbyRoute(
                viewModel = navController.pickupFlowViewModel(entry),
                onNavigate = navController::navigateToStep,
                onFlowFinished = onFlowFinished,
                onPickupRequested = onPickupRequested,
            )
        }

        composable<SpotDestination> { entry ->
            SpotRoute(
                viewModel = navController.pickupFlowViewModel(entry),
                onNavigate = navController::navigateToStep,
                onFlowFinished = onFlowFinished,
                onPickupRequested = onPickupRequested,
            )
        }

        composable<DrivingDestination> { entry ->
            DrivingRoute(
                viewModel = navController.pickupFlowViewModel(entry),
                onNavigate = navController::navigateToStep,
                onFlowFinished = onFlowFinished,
                onPickupRequested = onPickupRequested,
            )
        }

        composable<CompleteDestination> { entry ->
            CompleteRoute(
                viewModel = navController.pickupFlowViewModel(entry),
                onNavigate = navController::navigateToStep,
                onFlowFinished = onFlowFinished,
                onPickupRequested = onPickupRequested,
            )
        }

    }
}
