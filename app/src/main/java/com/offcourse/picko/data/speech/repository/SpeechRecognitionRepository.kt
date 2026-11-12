package com.offcourse.picko.data.speech.repository

import com.offcourse.picko.data.speech.model.SpeechRecognitionEvent
import kotlinx.coroutines.flow.Flow

interface SpeechRecognitionRepository {
    /**
     * 마이크를 켜고 픽업 요청 한 번을 듣는다. 수집하는 동안만 듣고, 수집을 멈추면 마이크를 끈다.
     * 최종 결과나 실패 이벤트를 내보낸 뒤 Flow 가 끝난다.
     */
    fun listen(): Flow<SpeechRecognitionEvent>
}
