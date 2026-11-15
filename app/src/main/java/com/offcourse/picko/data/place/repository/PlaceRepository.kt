package com.offcourse.picko.data.place.repository

import com.offcourse.picko.core.common.model.Coordinate

interface PlaceRepository {
    /**
     * 사용자가 말한 목적지 이름과 가장 잘 맞는 장소(1순위)의 좌표. [vehicle]에서 가까운 곳을 우선한다.
     * 맞는 장소가 없으면 null이다. 카카오 하루 한도에 닿았으면 [com.offcourse.picko.data.place.model.KakaoQuotaExceededException]을 던진다.
     */
    suspend fun findDestination(query: String, vehicle: Coordinate): Coordinate?
}
