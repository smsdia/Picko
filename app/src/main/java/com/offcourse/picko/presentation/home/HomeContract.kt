package com.offcourse.picko.presentation.home

import androidx.compose.runtime.Immutable

@Immutable
data class HomeState(
    // 저장된 값을 읽기 전이면 null. true 이면 시작 안내를, false 이면 음성 대기를 보여 준다.
    val isFirstLaunch: Boolean? = null,
)
