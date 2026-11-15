package com.offcourse.picko.data.speech.datasource

import kotlinx.coroutines.flow.Flow

/** 음성을 문장으로 바꾸는 엔진. 수집하는 동안만 듣고, 최종 결과나 실패 뒤에 끝난다. */
interface SpeechTextSource {
    fun listen(): Flow<SpeechTextEvent>
}
