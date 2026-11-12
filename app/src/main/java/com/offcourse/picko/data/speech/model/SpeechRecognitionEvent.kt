package com.offcourse.picko.data.speech.model

import androidx.compose.runtime.Immutable
import com.offcourse.picko.data.gleo.model.PickupRequestModel

/** 음성으로 픽업을 요청하는 한 번의 듣기에서 나오는 이벤트 */
sealed interface SpeechRecognitionEvent {
    /** 마이크가 켜져 말을 기다린다. */
    data object Listening : SpeechRecognitionEvent

    /** 말하는 도중의 인식 결과. 계속 바뀐다. */
    @Immutable
    data class Partial(val text: String) : SpeechRecognitionEvent

    /** 끝까지 인식해 탑승자와 장소를 읽었다. 듣기가 끝난다. */
    @Immutable
    data class Understood(val request: PickupRequestModel) : SpeechRecognitionEvent

    /** 끝까지 인식했지만 픽업 요청으로 읽지 못했다. 듣기가 끝난다. */
    @Immutable
    data class NotUnderstood(val text: String) : SpeechRecognitionEvent

    /** 듣기에 실패했다. 듣기가 끝난다. */
    @Immutable
    data class Failed(val error: SpeechRecognitionError) : SpeechRecognitionEvent
}

enum class SpeechRecognitionError {
    /** 말이 들어오지 않았거나 제한 시간 안에 끝나지 않았다. */
    Timeout,
    Network,
    Microphone,

    /** 클라이언트 ID/시크릿이 틀렸거나 STT 서비스 권한이 없다. */
    NotAuthorized,
    Unknown,
}
