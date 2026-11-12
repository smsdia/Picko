package com.offcourse.picko.data.speech.repositoryimpl

import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.speech.datasource.SpeechTextEvent
import com.offcourse.picko.data.speech.datasource.SpeechTextSource
import com.offcourse.picko.data.speech.datasource.SpokenPickupParser
import com.offcourse.picko.data.speech.model.SpeechRecognitionError
import com.offcourse.picko.data.speech.model.SpeechRecognitionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechRecognitionRepositoryImplTest {
    private fun repository(vararg events: SpeechTextEvent) = SpeechRecognitionRepositoryImpl(
        speechTextSource = object : SpeechTextSource {
            override fun listen(): Flow<SpeechTextEvent> = flowOf(*events)
        },
        spokenPickupParser = SpokenPickupParser(),
    )

    @Test
    fun 최종_문장을_픽업_요청으로_바꾼다() = runBlocking {
        val events = repository(
            SpeechTextEvent.Listening,
            SpeechTextEvent.Partial("엄마"),
            SpeechTextEvent.Final("엄마 강남역에서 픽업해줘"),
        ).listen().toList()

        assertEquals(
            listOf(
                SpeechRecognitionEvent.Listening,
                SpeechRecognitionEvent.Partial("엄마"),
                SpeechRecognitionEvent.Understood(PickupRequestModel(passenger = "엄마", place = "강남역")),
            ),
            events,
        )
    }

    @Test
    fun 픽업_요청으로_읽지_못하면_이해하지_못했다고_알린다() = runBlocking {
        val events = repository(SpeechTextEvent.Final("오늘 날씨 어때")).listen().toList()

        assertEquals(listOf(SpeechRecognitionEvent.NotUnderstood("오늘 날씨 어때")), events)
    }

    @Test
    fun 실패는_그대로_전한다() = runBlocking {
        val events = repository(SpeechTextEvent.Failed(SpeechRecognitionError.Network)).listen().toList()

        assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionError.Network)), events)
    }
}
