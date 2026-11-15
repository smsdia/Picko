package com.offcourse.picko.data.pickuplink.dto

import com.offcourse.picko.core.common.model.Coordinate
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private const val TYPE_VEHICLE_LOCATION = "vehicle_location"
private const val TYPE_PEDESTRIAN_LOCATION = "pedestrian_location"
private const val TYPE_SESSION_CANCELED = "session_canceled"

// Picko 서버 WebSocket(`/ws/vehicle`) 메시지. 서버 프로토콜이라 이름과 형식을 바꾸지 않는다.
//   송신: {"type":"vehicle_location","lat":<double>,"lng":<double>}
//   수신: {"type":"pedestrian_location","lat":<double>,"lng":<double>}, {"type":"session_canceled","reason":"<string>"}
@Serializable
data class PickoSocketMessageDto(
    val type: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val reason: String? = null,
)

/** 서버에서 받은 메시지 중 앱이 쓰는 것. */
sealed interface PickoSocketMessage {
    data class PedestrianLocation(val location: Coordinate) : PickoSocketMessage

    data class SessionCanceled(val reason: String?) : PickoSocketMessage
}

private val SocketJson = Json { ignoreUnknownKeys = true }

/** 차량 위치를 서버로 보내는 메시지(JSON 문자열). */
fun vehicleLocationMessage(location: Coordinate): String = SocketJson.encodeToString(
    PickoSocketMessageDto.serializer(),
    PickoSocketMessageDto(type = TYPE_VEHICLE_LOCATION, lat = location.latitude, lng = location.longitude),
)

/** 받은 문자열을 해석한다. 알 수 없는 `type`이거나 형식이 맞지 않으면 null이다(호출하는 쪽이 로그만 남기고 무시한다). */
fun parsePickoSocketMessage(text: String): PickoSocketMessage? {
    val dto = try {
        SocketJson.decodeFromString(PickoSocketMessageDto.serializer(), text)
    } catch (e: SerializationException) {
        return null
    }

    return when (dto.type) {
        TYPE_PEDESTRIAN_LOCATION -> {
            val lat = dto.lat ?: return null
            val lng = dto.lng ?: return null
            PickoSocketMessage.PedestrianLocation(Coordinate(latitude = lat, longitude = lng))
        }

        TYPE_SESSION_CANCELED -> PickoSocketMessage.SessionCanceled(dto.reason)

        else -> null
    }
}
