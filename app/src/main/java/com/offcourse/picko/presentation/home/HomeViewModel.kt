package com.offcourse.picko.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offcourse.picko.core.localstorage.onboarding.OnboardingStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val onboardingStorage: OnboardingStorage,
) : ViewModel() {
    val state: StateFlow<HomeState> = onboardingStorage.isFirstLaunch
        .map { isFirstLaunch -> HomeState(isFirstLaunch = isFirstLaunch) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            initialValue = HomeState(),
        )

    /** 시작 안내를 봤다고 기억한다. 저장되면 상태가 바뀌어 음성 대기 화면으로 넘어간다. */
    fun onStartClicked() {
        viewModelScope.launch { onboardingStorage.markOnboardingShown() }
    }
}
