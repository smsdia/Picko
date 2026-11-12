package com.offcourse.picko.data.speech.datasource

import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val LISTENING_DELAY_MILLIS = 800L
private const val PARTIAL_DELAY_MILLIS = 1_200L

/** 클라이언트 ID/시크릿이 없을 때 쓰는 가짜 엔진. 정해진 문장을 말한 것처럼 내보낸다. */
class FakeSpeechTextSource @Inject constructor() : SpeechTextSource {
    override fun listen(): Flow<SpeechTextEvent> = flow {
        emit(SpeechTextEvent.Listening)
        delay(LISTENING_DELAY_MILLIS)
        emit(SpeechTextEvent.Partial("엄마 강남역에서"))
        delay(PARTIAL_DELAY_MILLIS)
        emit(SpeechTextEvent.Final("엄마 강남역에서 픽업해줘"))
    }
}
