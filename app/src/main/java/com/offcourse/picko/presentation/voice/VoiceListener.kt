package com.offcourse.picko.presentation.voice

import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.speech.repository.SpeechSynthesisRepository
import com.offcourse.picko.domain.pickup.model.PickupListenEvent
import com.offcourse.picko.domain.pickup.usecase.ListenForPickupUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 안내를 말한 뒤 자동으로 다시 듣는 횟수의 상한. 주변 소리를 말로 잘못 들어 끝없이 되풀이하지 않게 한다.
private const val MAX_AUTO_RETRIES = 2

/**
 * 듣기 한 번의 수명과 [VoiceListenState]를 맡는다. 홈과 재요청 오버레이의 ViewModel이 각자 만들어 쓴다.
 * [start]를 다시 부르면 듣던 것을 멈추고 새로 듣는다. 요청을 읽으면 [onRequested]를 부른다.
 */
class VoiceListener(
    private val scope: CoroutineScope,
    private val listenForPickup: ListenForPickupUseCase,
    private val speechSynthesis: SpeechSynthesisRepository,
    private val onRequested: suspend (PickupRequestModel) -> Unit,
) {
    private val _state = MutableStateFlow(VoiceListenState())
    val state: StateFlow<VoiceListenState> = _state.asStateFlow()

    private var listenJob: Job? = null
    private var announceJob: Job? = null
    private var autoRetryCount = 0

    /** 사용자가 직접 시작하는 듣기. 자동 재시도 횟수를 처음부터 센다. */
    fun start() {
        announceJob?.cancel()
        autoRetryCount = 0
        listen()
    }

    /** 듣기와 안내를 멈추고 마이크를 끈다. 상태는 처음으로 돌린다. */
    fun stop() {
        announceJob?.cancel()
        announceJob = null
        listenJob?.cancel()
        listenJob = null
        _state.value = VoiceListenState()
    }

    /**
     * 안내 문구를 소리로 말하고, 소리가 났으면 자동으로 다시 듣는다. 연속 자동 재시도는 [MAX_AUTO_RETRIES]번까지다.
     * 이미 말하는 중이면 무시한다. 말하는 동안에는 마이크를 켜지 않아 안내 소리를 다시 듣지 않는다.
     */
    fun announceAndRetry(text: String) {
        if (announceJob?.isActive == true) return

        announceJob = scope.launch {
            val isSpoken = speechSynthesis.speak(text)

            if (isSpoken && autoRetryCount < MAX_AUTO_RETRIES) {
                autoRetryCount++
                listen()
            }
        }
    }

    private fun listen() {
        listenJob?.cancel()
        _state.value = VoiceListenState()

        listenJob = scope.launch {
            listenForPickup().collect { event -> onEvent(event) }
        }
    }

    private suspend fun onEvent(event: PickupListenEvent) {
        when (event) {
            PickupListenEvent.Listening ->
                _state.value = _state.value.copy(status = VoiceListenStatus.Listening)

            is PickupListenEvent.Partial ->
                _state.value = VoiceListenState(VoiceListenStatus.Listening, event.text)

            is PickupListenEvent.Requested -> onRequested(event.request)

            is PickupListenEvent.NotUnderstood ->
                _state.value = VoiceListenState(VoiceListenStatus.NotUnderstood, event.text)

            is PickupListenEvent.ContactNotFound ->
                _state.value = VoiceListenState(VoiceListenStatus.ContactNotFound)

            is PickupListenEvent.Failed ->
                _state.value = VoiceListenState(VoiceListenStatus.Failed(event.error))
        }
    }
}
