package com.offcourse.picko.data.speech.repositoryimpl

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import com.offcourse.picko.data.speech.repository.SpeechSynthesisRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

// 말하기가 끝났다는 콜백이 오지 않을 때 직접 끝내는 시간
private const val SPEAK_TIMEOUT_MILLIS = 15_000L

/**
 * Pleos TextToSpeech SDK 로 읽는다. 읽을 때마다 객체를 만들고 끝나면 해제한다.
 * 서버 합성은 앱 등록이 필요하고 STT 서버가 같은 키로 거부된 적이 있어, 등록이 필요 없는 온디바이스 모드를 쓴다.
 *
 * TODO: 읽을 때마다 초기화해서 시작이 느리면 객체를 재사용한다. 오디오 출력 위치(`setAudioZoneId`, `setAudioAttributes`)는
 *  문서에서 확인하지 못해 기본값을 쓴다.
 */
class PleosSpeechSynthesisRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechSynthesisRepository {
    override suspend fun speak(text: String): Boolean {
        val textToSpeech = TextToSpeech(context, Mode.ON_DEVICE)
        var listener: EventListener? = null

        return try {
            withTimeoutOrNull(SPEAK_TIMEOUT_MILLIS.milliseconds) {
                suspendCancellableCoroutine { continuation ->
                    fun finish(isSpoken: Boolean) {
                        if (continuation.isActive) continuation.resume(isSpoken)
                    }

                    val eventListener = object : EventListener {
                        override fun onReady() = Unit

                        override fun onStart() = Unit

                        override fun onUpdatedRms(rms: Double) = Unit

                        override fun onDone() = finish(true)

                        override fun onStop() = finish(false)

                        override fun onError(errorType: ErrorType) {
                            Timber.w("TTS error: %s", errorType)
                            finish(false)
                        }
                    }
                    listener = eventListener

                    textToSpeech.initialize(
                        object : OnInitializeListener {
                            override fun onSuccess() {
                                textToSpeech.setLocale(Locale.KOREA)
                                textToSpeech.addEventListener(eventListener)
                                textToSpeech.speak(text, true)
                            }

                            override fun onFailure(errorType: ErrorType) {
                                Timber.w("TTS initialize failed: %s", errorType)
                                finish(false)
                            }
                        },
                    )
                }
            } ?: false
        } finally {
            // 취소돼도 읽기를 멈추고 해제한다.
            runCatching {
                textToSpeech.stop()
                listener?.let(textToSpeech::removeEventListener)
                textToSpeech.release()
            }.onFailure { Timber.w(it, "TTS release failed") }
        }
    }
}
