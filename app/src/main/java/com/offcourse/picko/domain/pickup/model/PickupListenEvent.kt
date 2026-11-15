package com.offcourse.picko.domain.pickup.model

import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.speech.model.SpeechRecognitionError

/** 말로 하는 픽업 요청을 듣는 동안 나오는 이벤트. 듣기가 끝나는 이벤트는 [Requested] 이하 넷이다. */
sealed interface PickupListenEvent {
    data object Listening : PickupListenEvent

    /** 말하는 도중의 인식 결과 */
    data class Partial(val text: String) : PickupListenEvent

    /** 픽업 요청을 읽었고 탑승자 연락처가 있다. 연락처 조회에 실패했을 때(권한 없음 등)도 흐름이 이어서 처리한다. */
    data class Requested(val request: PickupRequestModel) : PickupListenEvent

    /** 끝까지 들었지만 픽업 요청으로 읽지 못했다. */
    data class NotUnderstood(val text: String) : PickupListenEvent

    /** 픽업 요청은 읽었지만 그 이름의 연락처가 없다. */
    data class ContactNotFound(val passenger: String) : PickupListenEvent

    data class Failed(val error: SpeechRecognitionError) : PickupListenEvent
}
