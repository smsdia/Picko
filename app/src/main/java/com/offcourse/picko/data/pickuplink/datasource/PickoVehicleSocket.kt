package com.offcourse.picko.data.pickuplink.datasource

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.network.PickoServerUrl
import com.offcourse.picko.core.network.di.PickoSocket
import com.offcourse.picko.data.pickuplink.dto.PickoSocketMessage
import com.offcourse.picko.data.pickuplink.dto.parsePickoSocketMessage
import com.offcourse.picko.data.pickuplink.dto.vehicleLocationMessage
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber

private const val SOCKET_PATH = "ws/vehicle"
private const val NORMAL_CLOSURE = 1000
private const val INVALID_TOKEN_REASON = "invalid_token"
private const val BACKOFF_START_MILLIS = 1_000L
private const val BACKOFF_MAX_MILLIS = 30_000L

/** 재접속을 기다리는 시간. 1초에서 시작해 두 배씩 늘리고 30초를 넘지 않는다. [attempt]는 0부터 센다. */
fun backoffMillis(attempt: Int): Long {
    val shift = attempt.coerceIn(0, 5)

    return (BACKOFF_START_MILLIS shl shift).coerceAtMost(BACKOFF_MAX_MILLIS)
}

/**
 * Picko 서버 WebSocket(`wss://<host>/ws/vehicle?token=<세션 토큰>`) 클라이언트. 인증은 토큰 쿼리뿐이다.
 * 서버는 상태를 보관하지 않아서 다시 접속해도 상대가 다음 위치를 보내기 전까지는 아무것도 오지 않는다.
 *
 * - 이미 연결 중이거나 연결된 같은 토큰으로 [connect]를 부르면 아무것도 하지 않는다.
 * - 실패하거나 정상 종료(1000)가 아니게 닫히면 [backoffMillis]만큼 기다려 다시 접속한다.
 * - `session_canceled`를 받았거나, 서버가 `invalid_token`으로 닫았거나, [close]를 부르면 다시 접속하지 않는다.
 */
@Singleton
class PickoVehicleSocket @Inject constructor(
    @param:PickoSocket private val client: OkHttpClient,
) : PickoSessionSocket {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    private val _state = MutableStateFlow(SocketState())
    override val state: StateFlow<SocketState> = _state.asStateFlow()

    // 아래 값은 lock 안에서만 읽고 쓴다.
    private var token: String? = null
    private var socket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var attempt = 0
    private var isStopped = true

    override fun connect(token: String) {
        synchronized(lock) {
            if (!isStopped && this.token == token) return

            // 다른 세션의 소켓이 남아 있으면 닫고 새로 시작한다.
            stopLocked()
            this.token = token
            isStopped = false
            attempt = 0
            _state.value = SocketState()
            openLocked()
        }
    }

    override fun sendVehicleLocation(location: Coordinate): Boolean {
        val current = synchronized(lock) { socket } ?: return false

        return current.send(vehicleLocationMessage(location))
    }

    override fun close() {
        synchronized(lock) { stopLocked() }
    }

    private fun stopLocked() {
        isStopped = true
        reconnectJob?.cancel()
        reconnectJob = null
        socket?.close(NORMAL_CLOSURE, null)
        socket = null
        token = null
    }

    private fun openLocked() {
        val currentToken = token ?: return
        // 토큰은 주소의 쿼리에만 싣는다. 이 클라이언트는 요청 로그를 남기지 않는다.
        val url = (PickoServerUrl + SOCKET_PATH).toHttpUrl()
            .newBuilder()
            .addQueryParameter("token", currentToken)
            .build()

        socket = client.newWebSocket(Request.Builder().url(url).build(), Listener())
    }

    private fun scheduleReconnect(failed: WebSocket) {
        synchronized(lock) {
            // 이미 닫았거나 다른 소켓으로 바뀐 뒤에 온 콜백은 무시한다.
            if (isStopped || socket !== failed) return

            socket = null
            val wait = backoffMillis(attempt++)
            reconnectJob = scope.launch {
                delay(wait)
                synchronized(lock) {
                    if (!isStopped && socket == null) openLocked()
                }
            }
        }
    }

    private fun end(reason: SocketEnd) {
        synchronized(lock) { stopLocked() }
        _state.update { it.copy(end = reason) }
    }

    private inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            synchronized(lock) {
                if (socket === webSocket) attempt = 0
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            when (val message = parsePickoSocketMessage(text)) {
                is PickoSocketMessage.PedestrianLocation -> _state.update { it.copy(pedestrian = message.location) }

                is PickoSocketMessage.SessionCanceled -> {
                    Timber.i("서버가 세션을 취소했다")
                    end(SocketEnd.SessionCanceled)
                }

                // 알 수 없는 type이나 형식은 내용을 남기지 않고 무시한다(위치가 들어 있을 수 있다).
                null -> Timber.w("알 수 없는 WebSocket 메시지를 무시한다")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(NORMAL_CLOSURE, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            when {
                reason == INVALID_TOKEN_REASON -> end(SocketEnd.InvalidToken)

                // 정상 종료는 다시 접속하지 않는다.
                code == NORMAL_CLOSURE -> synchronized(lock) {
                    if (socket === webSocket) socket = null
                }

                else -> {
                    Timber.w("WebSocket이 닫혔다: %d", code)
                    scheduleReconnect(webSocket)
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            val code = response?.code
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN) {
                Timber.w("WebSocket 접속이 거절됐다: HTTP %d", code)
                end(SocketEnd.InvalidToken)
                return
            }

            Timber.w(t, "WebSocket 연결 실패")
            scheduleReconnect(webSocket)
        }
    }
}
