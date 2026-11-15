package com.offcourse.picko.domain.pickup.usecase

import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.contact.repository.ContactRepository
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.speech.model.SpeechRecognitionError
import com.offcourse.picko.data.speech.model.SpeechRecognitionEvent
import com.offcourse.picko.data.speech.repository.SpeechRecognitionRepository
import com.offcourse.picko.domain.pickup.model.PickupListenEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ListenForPickupUseCaseTest {
    private val request = PickupRequestModel(passenger = "현대", place = "대치동 학원가")

    private fun useCase(
        vararg events: SpeechRecognitionEvent,
        lookup: () -> ContactLookupResult = { ContactLookupResult.Found(ContactModel("현대", "010-1111-2222")) },
    ) = ListenForPickupUseCase(
        speechRecognitionRepository = object : SpeechRecognitionRepository {
            override fun listen(): Flow<SpeechRecognitionEvent> = flowOf(*events)
        },
        contactRepository = object : ContactRepository {
            override suspend fun findByName(name: String): ContactLookupResult = lookup()
        },
    )

    @Test
    fun 연락처가_있으면_요청으로_알린다() = runBlocking {
        val events = useCase(SpeechRecognitionEvent.Understood(request)).invoke().toList()

        assertEquals(listOf(PickupListenEvent.Requested(request)), events)
    }

    @Test
    fun 연락처가_없으면_흐름을_시작하지_않고_연락처_없음으로_알린다() = runBlocking {
        val events = useCase(
            SpeechRecognitionEvent.Understood(request),
            lookup = { ContactLookupResult.NotFound },
        ).invoke().toList()

        assertEquals(listOf(PickupListenEvent.ContactNotFound("현대")), events)
    }

    @Test
    fun 연락처를_조회하지_못하면_흐름이_이어서_처리하도록_요청으로_알린다() = runBlocking {
        val events = useCase(
            SpeechRecognitionEvent.Understood(request),
            lookup = { throw SecurityException() },
        ).invoke().toList()

        assertEquals(listOf(PickupListenEvent.Requested(request)), events)
    }

    @Test
    fun 듣는_도중_이벤트와_실패는_그대로_전한다() = runBlocking {
        val events = useCase(
            SpeechRecognitionEvent.Listening,
            SpeechRecognitionEvent.Partial("현대"),
            SpeechRecognitionEvent.NotUnderstood("오늘 날씨"),
            SpeechRecognitionEvent.Failed(SpeechRecognitionError.Network),
        ).invoke().toList()

        assertEquals(
            listOf(
                PickupListenEvent.Listening,
                PickupListenEvent.Partial("현대"),
                PickupListenEvent.NotUnderstood("오늘 날씨"),
                PickupListenEvent.Failed(SpeechRecognitionError.Network),
            ),
            events,
        )
    }
}
