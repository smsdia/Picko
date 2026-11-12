package com.offcourse.picko.domain.pickup.usecase

import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.contact.repository.ContactRepository
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.speech.model.SpeechRecognitionEvent
import com.offcourse.picko.data.speech.repository.SpeechRecognitionRepository
import com.offcourse.picko.domain.pickup.model.PickupListenEvent
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 말로 하는 픽업 요청을 한 번 듣고, 읽은 탑승자의 연락처가 있는지까지 확인한다.
 * 연락처가 없으면 흐름을 시작하지 않고 [PickupListenEvent.ContactNotFound]로 알려, 듣던 화면에 머물게 한다.
 */
class ListenForPickupUseCase @Inject constructor(
    private val speechRecognitionRepository: SpeechRecognitionRepository,
    private val contactRepository: ContactRepository,
) {
    operator fun invoke(): Flow<PickupListenEvent> = speechRecognitionRepository.listen().map { event ->
        when (event) {
            SpeechRecognitionEvent.Listening -> PickupListenEvent.Listening

            is SpeechRecognitionEvent.Partial -> PickupListenEvent.Partial(event.text)

            is SpeechRecognitionEvent.Understood -> checkContact(event.request)

            is SpeechRecognitionEvent.NotUnderstood -> PickupListenEvent.NotUnderstood(event.text)

            is SpeechRecognitionEvent.Failed -> PickupListenEvent.Failed(event.error)
        }
    }

    private suspend fun checkContact(request: PickupRequestModel): PickupListenEvent {
        val lookup = try {
            contactRepository.findByName(request.passenger)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 권한이 없는 등 조회하지 못한 경우는 흐름의 첫 화면이 안내와 권한 요청을 맡는다.
            return PickupListenEvent.Requested(request)
        }

        return if (lookup == ContactLookupResult.NotFound) {
            PickupListenEvent.ContactNotFound(request.passenger)
        } else {
            PickupListenEvent.Requested(request)
        }
    }
}
