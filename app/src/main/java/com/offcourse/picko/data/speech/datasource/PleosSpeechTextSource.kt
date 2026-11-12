package com.offcourse.picko.data.speech.datasource

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.OnInitializeListener
import ai.pleos.playground.stt.listener.OnServerConnectionListener
import ai.pleos.playground.stt.listener.ResultListener
import android.content.Context
import com.offcourse.picko.BuildConfig
import com.offcourse.picko.data.speech.model.SpeechRecognitionError
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import timber.log.Timber

// 말이 없거나 서버 응답이 오지 않을 때 직접 끝내는 시간. SDK 의 TIMEOUT 오류가 먼저 오면 그것을 쓴다.
private const val LISTEN_TIMEOUT_MILLIS = 15_000L

/**
 * Pleos SpeechToText SDK 로 듣는다. 듣기마다 객체를 만들고 초기화, 앱 등록, 인식을 거쳐 끝나면 해제한다.
 * 서버 모드(HYBRID)라 클라이언트 ID/시크릿이 필요하다. 값이 없으면 [FakeSpeechTextSource]를 쓴다.
 *
 * TODO: 듣기마다 초기화와 앱 등록을 다시 해서 시작이 느리면 객체를 재사용한다.
 */
class PleosSpeechTextSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechTextSource {
    override fun listen(): Flow<SpeechTextEvent> = callbackFlow {
        val speechToText = SpeechToText(context, Mode.HYBRID)
        var lastText = ""

        fun finishWith(event: SpeechTextEvent) {
            trySend(event)
            close()
        }

        val resultListener = object : ResultListener {
            // 마이크가 준비돼 말을 기다린다. 말을 시작하기 전에도 "듣고 있어요"를 보여 준다.
            override fun onReady() {
                trySend(SpeechTextEvent.Listening)
            }

            override fun onStartedRecognition() {
                trySend(SpeechTextEvent.Listening)
            }

            override fun onUpdated(stt: String, completed: Boolean) {
                lastText = stt
                if (completed) finishWith(SpeechTextEvent.Final(stt)) else trySend(SpeechTextEvent.Partial(stt))
            }

            override fun onUpdatedEpdData(on: Long, off: Long) = Unit

            override fun onUpdatedRms(rms: Double) = Unit

            // 최종 결과보다 먼저 올 수 있어, 그때까지 받은 문장을 최종으로 본다. 말이 없었으면 시간 초과로 본다.
            override fun onEndedRecognition() {
                finishWith(
                    if (lastText.isBlank()) {
                        SpeechTextEvent.Failed(SpeechRecognitionError.Timeout)
                    } else {
                        SpeechTextEvent.Final(lastText)
                    },
                )
            }

            override fun onError(errorType: ErrorType) {
                Timber.w("STT error: %s", errorType)
                finishWith(SpeechTextEvent.Failed(errorType.toError()))
            }
        }

        speechToText.initialize(
            object : OnInitializeListener {
                override fun onSuccess() {
                    speechToText.registerApp(
                        BuildConfig.PLEOS_CLIENT_ID,
                        BuildConfig.PLEOS_CLIENT_SECRET,
                        object : OnServerConnectionListener {
                            override fun onConnected() {
                                speechToText.setLocale(Locale.KOREA)
                                speechToText.addListener(resultListener)
                                speechToText.request()
                            }

                            override fun onFailed(errorType: ErrorType) {
                                Timber.w("STT register failed: %s", errorType)
                                finishWith(SpeechTextEvent.Failed(errorType.toError()))
                            }
                        },
                    )
                }

                override fun onFailure(errorType: ErrorType) {
                    Timber.w("STT initialize failed: %s", errorType)
                    finishWith(SpeechTextEvent.Failed(errorType.toError()))
                }
            },
        )

        launch {
            delay(LISTEN_TIMEOUT_MILLIS)
            finishWith(SpeechTextEvent.Failed(SpeechRecognitionError.Timeout))
        }

        awaitClose {
            runCatching {
                speechToText.stop()
                speechToText.removeListener(resultListener)
                speechToText.release()
            }.onFailure { Timber.w(it, "STT release failed") }
        }
    }
}

private fun ErrorType.toError(): SpeechRecognitionError = when (this) {
    ErrorType.TIMEOUT -> SpeechRecognitionError.Timeout

    ErrorType.NETWORK_ERROR -> SpeechRecognitionError.Network

    ErrorType.MIC_ERROR, ErrorType.AUDIO_ERROR -> SpeechRecognitionError.Microphone

    ErrorType.UNAUTHENTICATED, ErrorType.PERMISSION_DENIED -> SpeechRecognitionError.NotAuthorized

    else -> SpeechRecognitionError.Unknown
}
