package com.offcourse.picko.data.pickuplink.datasource

import com.offcourse.picko.core.common.model.Coordinate
import kotlinx.coroutines.flow.StateFlow

/** 소켓을 더 이어 가지 않는 이유. 이 중 하나가 되면 재접속하지 않는다. */
enum class SocketEnd {
    /** 서버가 `session_canceled`를 보냈다. */
    SessionCanceled,

    /** 서버가 토큰이 유효하지 않다고 닫았다. */
    InvalidToken,
}

/** 소켓에서 받은 값. [pedestrian]은 탑승자가 위치를 보내기 전에는 null이다. */
data class SocketState(
    val pedestrian: Coordinate? = null,
    val end: SocketEnd? = null,
)

/** 세션 토큰으로 서버와 실시간으로 위치를 주고받는 연결. */
interface PickoSessionSocket {
    val state: StateFlow<SocketState>

    /** [token]으로 접속한다. 이미 연결 중이거나 연결된 같은 토큰이면 아무것도 하지 않는다. */
    fun connect(token: String)

    /** 연결되어 있으면 차량 위치를 보내고 true. 연결 중이거나 끊겨 있으면 보내지 않고 false. */
    fun sendVehicleLocation(location: Coordinate): Boolean

    /** 연결을 닫고 재접속을 멈춘다. */
    fun close()
}
