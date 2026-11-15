package com.offcourse.picko.presentation.main

import com.offcourse.picko.data.gleo.model.PickupRequestModel

sealed interface MainSideEffect {
    /** Gleo 로 받은 픽업 요청을 읽었다. 새 픽업 흐름을 시작한다. */
    data class StartPickup(val request: PickupRequestModel) : MainSideEffect
}
