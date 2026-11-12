package com.offcourse.picko.presentation.home

import androidx.compose.runtime.Immutable
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.voice.VoiceListenState

@Immutable
data class HomeState(
    // 저장된 값을 읽기 전이면 null. true 이면 시작 안내를, false 이면 음성 대기를 보여 준다.
    val isFirstLaunch: Boolean? = null,
    val voice: VoiceListenState = VoiceListenState(),
)

sealed interface HomeSideEffect {
    /** 말로 픽업 요청을 읽었고 연락처도 있다. 새 픽업 흐름을 시작한다. */
    data class StartPickup(val request: PickupRequestModel) : HomeSideEffect
}
