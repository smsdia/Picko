package com.offcourse.picko.data.place.repository

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.place.model.PlaceModel

interface PlaceRepository {
    /** 사용자가 말한 장소 이름과 맞는 장소들. 맞는 장소가 없으면 빈 목록이다. */
    suspend fun searchPlaces(query: String): List<PlaceModel>

    /** 탑승자의 위치를 반영해 안전하게 정차할 수 있는 장소를 추천한다. 가까운 순서이다. */
    suspend fun recommendNearby(passenger: Coordinate): List<PlaceModel>
}
