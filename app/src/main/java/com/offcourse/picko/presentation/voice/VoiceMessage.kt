package com.offcourse.picko.presentation.voice

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.offcourse.picko.R
import com.offcourse.picko.data.speech.model.SpeechRecognitionError

/**
 * 마이크 아래 문구. 인식한 문장이 있으면 그것을, 없으면 상태에 맞는 안내를 보여 준다.
 * [example]은 아직 듣지 않을 때 보여 줄 예시 문구다.
 * TODO(figma): 듣는 중, 실패 문구와 표시는 시안이 없다. 지금은 예시 문구 자리에 같은 스타일로 보여 준다.
 */
@Composable
fun voiceMessage(voice: VoiceListenState, example: String): String {
    val recognized = voice.recognizedText.takeIf { it.isNotBlank() }

    return when (val status = voice.status) {
        VoiceListenStatus.Idle -> example

        VoiceListenStatus.Listening ->
            recognized?.let { "“$it”" } ?: stringResource(R.string.home_voice_listening)

        VoiceListenStatus.NotUnderstood -> stringResource(R.string.home_voice_not_understood)

        VoiceListenStatus.ContactNotFound -> stringResource(R.string.home_voice_contact_not_found)

        is VoiceListenStatus.Failed -> when (status.error) {
            SpeechRecognitionError.Timeout -> stringResource(R.string.home_voice_timeout)
            SpeechRecognitionError.Network -> stringResource(R.string.home_voice_network)
            SpeechRecognitionError.Microphone -> stringResource(R.string.home_voice_microphone)
            SpeechRecognitionError.NotAuthorized, SpeechRecognitionError.Unknown ->
                stringResource(R.string.home_voice_failed)
        }
    }
}
