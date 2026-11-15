package com.offcourse.picko.data.speech.datasource

import com.offcourse.picko.data.speech.model.SpeechRecognitionError

/** 음성 인식 엔진이 내보내는 문장 단위 이벤트. 문장을 해석하기 전의 값이다. */
sealed interface SpeechTextEvent {
    data object Listening : SpeechTextEvent

    data class Partial(val text: String) : SpeechTextEvent

    data class Final(val text: String) : SpeechTextEvent

    data class Failed(val error: SpeechRecognitionError) : SpeechTextEvent
}
