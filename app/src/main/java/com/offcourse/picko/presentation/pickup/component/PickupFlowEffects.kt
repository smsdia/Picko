package com.offcourse.picko.presentation.pickup.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.pickup.PickupFlowSideEffect
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep

/**
 * 흐름 ViewModel이 보내는 이동 이벤트를 받아 호출 쪽 람다로 넘긴다.
 * 화면이 `RESUMED`일 때만 수집한다. 같은 ViewModel을 쓰는 이전 화면이 전환 중에 이벤트를 가로채지 않게 하고,
 * 그 사이 보낸 이벤트는 새 화면이 활성화되면 전달된다.
 */
@Composable
internal fun PickupFlowEffects(
    viewModel: PickupFlowViewModel,
    onNavigate: (PickupStep) -> Unit,
    onFlowFinished: () -> Unit,
    onPickupRequested: (PickupRequestModel) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnNavigate = rememberUpdatedState(onNavigate)
    val currentOnFlowFinished = rememberUpdatedState(onFlowFinished)
    val currentOnPickupRequested = rememberUpdatedState(onPickupRequested)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.sideEffect.collect { effect ->
                when (effect) {
                    is PickupFlowSideEffect.NavigateTo -> currentOnNavigate.value(effect.step)
                    PickupFlowSideEffect.FinishFlow -> currentOnFlowFinished.value()
                    is PickupFlowSideEffect.StartPickup -> currentOnPickupRequested.value(effect.request)
                }
            }
        }
    }
}
