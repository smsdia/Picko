package com.offcourse.picko.presentation.voice

import androidx.compose.runtime.Immutable
import com.offcourse.picko.data.speech.model.SpeechRecognitionError

/** 말로 하는 픽업 요청을 듣는 화면(홈, 재요청 오버레이)의 상태. [recognizedText]는 듣는 도중 인식된 문장이다. */
@Immutable
data class VoiceListenState(
    val status: VoiceListenStatus = VoiceListenStatus.Idle,
    val recognizedText: String = "",
)

sealed interface VoiceListenStatus {
    /** 아직 듣지 않는다. 예시 문구를 보여 준다. */
    data object Idle : VoiceListenStatus

    data object Listening : VoiceListenStatus

    /** 끝까지 들었지만 픽업 요청으로 읽지 못했다. */
    data object NotUnderstood : VoiceListenStatus

    /** 요청은 읽었지만 그 이름의 연락처가 없다. */
    data object ContactNotFound : VoiceListenStatus

    data class Failed(val error: SpeechRecognitionError) : VoiceListenStatus
}
