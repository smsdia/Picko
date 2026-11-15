package com.offcourse.picko.data.place.repository

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.place.model.PlaceModel

interface PickupRecommendRepository {
    /**
     * 차량의 현재 위치 [vehicle]에서 목적지 [destination]으로 가는 길의 픽업 후보. 서버가 점수를 매긴 순서이다.
     * 후보가 없으면 빈 목록이고, 서버에 닿지 못하거나 응답을 읽지 못하면 [com.offcourse.picko.data.place.model.PickupRecommendException]을 던진다.
     */
    suspend fun recommend(vehicle: Coordinate, destination: Coordinate): List<PlaceModel>
}
