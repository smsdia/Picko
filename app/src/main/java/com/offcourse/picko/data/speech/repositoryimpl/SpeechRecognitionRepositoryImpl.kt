package com.offcourse.picko.data.speech.repositoryimpl

import com.offcourse.picko.data.speech.datasource.SpeechTextEvent
import com.offcourse.picko.data.speech.datasource.SpeechTextSource
import com.offcourse.picko.data.speech.datasource.SpokenPickupParser
import com.offcourse.picko.data.speech.model.SpeechRecognitionEvent
import com.offcourse.picko.data.speech.repository.SpeechRecognitionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SpeechRecognitionRepositoryImpl @Inject constructor(
    private val speechTextSource: SpeechTextSource,
    private val spokenPickupParser: SpokenPickupParser,
) : SpeechRecognitionRepository {
    override fun listen(): Flow<SpeechRecognitionEvent> = speechTextSource.listen().map { event ->
        when (event) {
            SpeechTextEvent.Listening -> SpeechRecognitionEvent.Listening

            is SpeechTextEvent.Partial -> SpeechRecognitionEvent.Partial(event.text)

            is SpeechTextEvent.Final -> spokenPickupParser.parse(event.text)
                ?.let { SpeechRecognitionEvent.Understood(it) }
                ?: SpeechRecognitionEvent.NotUnderstood(event.text)

            is SpeechTextEvent.Failed -> SpeechRecognitionEvent.Failed(event.error)
        }
    }
}
