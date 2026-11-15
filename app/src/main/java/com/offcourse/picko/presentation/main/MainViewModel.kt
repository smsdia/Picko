package com.offcourse.picko.presentation.main

import androidx.lifecycle.ViewModel
import com.offcourse.picko.data.gleo.model.GleoActionResponseModel
import com.offcourse.picko.data.gleo.model.GleoActionStatus
import com.offcourse.picko.data.gleo.repository.GleoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Gleo 호출을 받아 읽는다. 읽은 요청은 [MainSideEffect.StartPickup]으로 내보내고, 그 뒤의 처리와 Gleo 콜백은
 * 새로 시작되는 픽업 흐름이 맡는다. 요청을 읽지 못했으면 여기서 실패 콜백을 보낸다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val gleoRepository: GleoRepository,
) : ViewModel() {
    private val _sideEffect = Channel<MainSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<MainSideEffect> = _sideEffect.receiveAsFlow()

    fun onPickupRequested(actionParam: String?) {
        val request = gleoRepository.parsePickupRequest(actionParam)

        if (request == null) {
            gleoRepository.sendPickupResponse(
                GleoActionResponseModel(
                    status = GleoActionStatus.FAIL,
                    message = "I could not understand the pickup request.",
                ),
            )
            return
        }

        _sideEffect.trySend(MainSideEffect.StartPickup(request))
    }
}
