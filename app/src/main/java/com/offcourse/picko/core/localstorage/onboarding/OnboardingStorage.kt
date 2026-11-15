package com.offcourse.picko.core.localstorage.onboarding

import kotlinx.coroutines.flow.Flow

/** 시작 안내(온보딩)를 이미 봤는지 기억한다. */
interface OnboardingStorage {
    /** 앱을 처음 연 것이면 true. 저장된 값을 읽지 못하면 처음으로 본다. */
    val isFirstLaunch: Flow<Boolean>

    suspend fun markOnboardingShown()
}
