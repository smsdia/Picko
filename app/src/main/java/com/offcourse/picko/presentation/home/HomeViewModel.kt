package com.offcourse.picko.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offcourse.picko.core.localstorage.onboarding.OnboardingStorage
import com.offcourse.picko.data.speech.repository.SpeechSynthesisRepository
import com.offcourse.picko.domain.pickup.usecase.ListenForPickupUseCase
import com.offcourse.picko.presentation.voice.VoiceListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val onboardingStorage: OnboardingStorage,
    listenForPickup: ListenForPickupUseCase,
    speechSynthesis: SpeechSynthesisRepository,
) : ViewModel() {
    private val _sideEffect = Channel<HomeSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<HomeSideEffect> = _sideEffect.receiveAsFlow()

    private val voiceListener = VoiceListener(
        scope = viewModelScope,
        listenForPickup = listenForPickup,
        speechSynthesis = speechSynthesis,
        onRequested = { request -> _sideEffect.send(HomeSideEffect.StartPickup(request)) },
    )

    val state: StateFlow<HomeState> = combine(onboardingStorage.isFirstLaunch, voiceListener.state) { isFirstLaunch, voice ->
        HomeState(isFirstLaunch = isFirstLaunch, voice = voice)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
        initialValue = HomeState(),
    )

    /** 시작 안내를 봤다고 기억한다. 저장되면 상태가 바뀌어 음성 대기 화면으로 넘어간다. */
    fun onStartClicked() {
        viewModelScope.launch { onboardingStorage.markOnboardingShown() }
    }

    /** 음성 대기 화면이 보이기 시작했다. 바로 듣는다. */
    fun onVoiceVisible() {
        voiceListener.start()
    }

    /** 음성 대기 화면이 가려졌다. 마이크를 끈다. */
    fun onVoiceHidden() {
        voiceListener.stop()
    }

    /** 마이크를 눌러 다시 듣는다. */
    fun onMicClicked() {
        voiceListener.start()
    }

    /** 다시 말해야 하는 상태가 됐다. 안내를 소리로 읽고 다시 듣는다. */
    fun onVoiceAnnounce(text: String) {
        voiceListener.announceAndRetry(text)
    }
}
