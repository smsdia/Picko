package com.offcourse.picko.core.common.util

import androidx.compose.runtime.Immutable

/** 비동기 데이터를 화면에서 다루기 위한 래퍼. 화면 전체 상태(`XxxState`)와 구분한다. */
@Immutable
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    data class Error(val throwable: Throwable) : UiState<Nothing>
}
