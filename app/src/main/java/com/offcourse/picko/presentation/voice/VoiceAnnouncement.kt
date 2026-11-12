package com.offcourse.picko.presentation.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.offcourse.picko.R

/**
 * 다시 말해야 하는 상태(연락처를 못 찾음, 알아듣지 못함)가 되면 안내 문구를 [onAnnounce]로 넘겨 소리로 읽게 한다.
 * 문구는 화면 문구와 같은 `strings.xml`에서 가져온다. 마이크를 눌러야 하는 실패(네트워크, 마이크 등)는 읽지 않는다.
 */
@Composable
fun VoiceAnnouncement(
    voice: VoiceListenState,
    onAnnounce: (String) -> Unit,
) {
    val text = when (voice.status) {
        VoiceListenStatus.ContactNotFound -> stringResource(R.string.home_voice_contact_not_found)
        VoiceListenStatus.NotUnderstood -> stringResource(R.string.voice_announce_not_understood)
        else -> null
    }
    val currentText by rememberUpdatedState(text)
    val currentOnAnnounce by rememberUpdatedState(onAnnounce)

    LaunchedEffect(voice) {
        currentText?.let(currentOnAnnounce)
    }
}
