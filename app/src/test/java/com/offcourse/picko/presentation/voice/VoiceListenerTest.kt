package com.offcourse.picko.presentation.voice

import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.contact.repository.ContactRepository
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.speech.model.SpeechRecognitionEvent
import com.offcourse.picko.data.speech.repository.SpeechRecognitionRepository
import com.offcourse.picko.data.speech.repository.SpeechSynthesisRepository
import com.offcourse.picko.domain.pickup.usecase.ListenForPickupUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceListenerTest {
    private var listenCount = 0
    private val spoken = mutableListOf<String>()

    // 듣기마다 "연락처 없는 이름"을 읽은 것으로 내보낸다.
    private fun listener(isSpoken: Boolean = true) = VoiceListener(
        scope = CoroutineScope(Dispatchers.Unconfined),
        listenForPickup = ListenForPickupUseCase(
            speechRecognitionRepository = object : SpeechRecognitionRepository {
                override fun listen(): Flow<SpeechRecognitionEvent> {
                    listenCount++
                    return flowOf(SpeechRecognitionEvent.Understood(PickupRequestModel(passenger = "현대")))
                }
            },
            contactRepository = object : ContactRepository {
                override suspend fun findByName(name: String) = ContactLookupResult.NotFound
            },
        ),
        speechSynthesis = object : SpeechSynthesisRepository {
            override suspend fun speak(text: String): Boolean {
                spoken += text
                return isSpoken
            }
        },
        onRequested = {},
    )

    @Test
    fun 연락처가_없으면_상태가_연락처_없음이_된다() {
        val listener = listener()

        listener.start()

        assertEquals(VoiceListenStatus.ContactNotFound, listener.state.value.status)
    }

    @Test
    fun 안내를_말한_뒤_자동으로_다시_듣고_두_번까지만_되풀이한다() {
        val listener = listener()

        listener.start()
        repeat(5) { listener.announceAndRetry("안내") }

        // 직접 시작 1번 + 자동 재시도 2번
        assertEquals(3, listenCount)
        assertEquals(5, spoken.size)
    }

    @Test
    fun 소리가_나지_않으면_다시_듣지_않는다() {
        val listener = listener(isSpoken = false)

        listener.start()
        listener.announceAndRetry("안내")

        assertEquals(1, listenCount)
    }

    @Test
    fun 직접_다시_시작하면_자동_재시도_횟수를_처음부터_센다() {
        val listener = listener()

        listener.start()
        repeat(3) { listener.announceAndRetry("안내") }
        listener.start()
        repeat(3) { listener.announceAndRetry("안내") }

        // (1 + 2) + (1 + 2)
        assertEquals(6, listenCount)
    }

    @Test
    fun 멈추면_상태를_처음으로_돌린다() {
        val listener = listener()

        listener.start()
        listener.stop()

        assertEquals(VoiceListenState(), listener.state.value)
    }
}
