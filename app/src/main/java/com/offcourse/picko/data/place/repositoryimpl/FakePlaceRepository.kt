package com.offcourse.picko.data.place.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.place.repository.PlaceRepository
import javax.inject.Inject

/** 카카오 키가 없을 때의 가짜 구현. 질의와 위치에 상관없이 서울역 근처 좌표를 돌려준다. */
class FakePlaceRepository @Inject constructor() : PlaceRepository {
    override suspend fun findDestination(query: String, vehicle: Coordinate): Coordinate =
        Coordinate(latitude = 37.5550, longitude = 126.9724)
}
