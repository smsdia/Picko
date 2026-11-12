package com.offcourse.picko.data.gleo.repository

import com.offcourse.picko.data.gleo.model.GleoActionResponseModel
import com.offcourse.picko.data.gleo.model.PickupRequestModel

interface GleoRepository {
    /**
     * REQUEST_PICKUP 호출의 ACTION_PARAM JSON 을 픽업 요청으로 바꾼다.
     * 형식이 틀리거나 탑승자 이름이 비어 있으면 null 을 돌려준다.
     */
    fun parsePickupRequest(actionParam: String?): PickupRequestModel?

    /** REQUEST_PICKUP 의 처리 결과를 Gleo 에 알린다. 호출 후 20초 안에 보내지 않으면 Gleo 가 지연 안내를 한다. */
    fun sendPickupResponse(response: GleoActionResponseModel)
}
