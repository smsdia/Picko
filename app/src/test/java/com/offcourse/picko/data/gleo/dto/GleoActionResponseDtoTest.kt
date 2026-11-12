package com.offcourse.picko.data.gleo.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class GleoActionResponseDtoTest {
    @Test
    fun 문서의_콜백_JSON_형식으로_직렬화한다() {
        val dto = GleoActionResponseDto(
            functionName = "REQUEST_PICKUP",
            status = GleoActionResponseDto.Status.ASKING,
            message = "There are 2 contacts named Dad.",
            mode = "voice",
        )

        assertEquals(
            """{"functionName":"REQUEST_PICKUP","status":"asking","message":"There are 2 contacts named Dad.","mode":"voice"}""",
            Json.encodeToString(dto),
        )
    }
}
