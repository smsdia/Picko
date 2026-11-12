package com.offcourse.picko.data.gleo.datasource

import com.offcourse.picko.data.gleo.dto.PickupRequestDto
import com.offcourse.picko.data.gleo.dto.toModel
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class PickupRequestParser @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    /** 형식이 틀리거나 탑승자 이름이 비어 있으면 null */
    fun parse(actionParam: String?): PickupRequestModel? {
        if (actionParam.isNullOrBlank()) return null

        val dto = try {
            json.decodeFromString<PickupRequestDto>(actionParam)
        } catch (e: SerializationException) {
            return null
        } catch (e: IllegalArgumentException) {
            return null
        }

        return dto.toModel()
    }
}
