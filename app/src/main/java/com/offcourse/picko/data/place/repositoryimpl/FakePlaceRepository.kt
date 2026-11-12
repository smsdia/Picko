package com.offcourse.picko.data.place.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.place.model.PickupReason
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.place.repository.PlaceRepository
import javax.inject.Inject

/** 장소 검색 연동이 정해지기 전에 쓰는 가짜 구현. 질의와 위치에 상관없이 시안의 예시 목록을 돌려준다. */
class FakePlaceRepository @Inject constructor() : PlaceRepository {
    override suspend fun searchPlaces(query: String): List<PlaceModel> = listOf(
        PlaceModel(
            id = "search-1",
            name = "대치동 학원가 사거리",
            coordinate = Coordinate(37.4946, 127.0576),
            distanceKm = 18,
            durationMinutes = 20,
            tollFeeWon = 3_000,
        ),
        PlaceModel(
            id = "search-2",
            name = "대치역 2번 출구",
            coordinate = Coordinate(37.4947, 127.0631),
            distanceKm = 18,
            durationMinutes = 20,
        ),
        PlaceModel(
            id = "search-3",
            name = "한티역 학원가 갓길",
            coordinate = Coordinate(37.4962, 127.0529),
            distanceKm = 18,
            durationMinutes = 20,
            tollFeeWon = 3_000,
        ),
    )

    override suspend fun recommendNearby(passenger: Coordinate): List<PlaceModel> = listOf(
        PlaceModel(
            id = "nearby-1",
            name = "대치역 2번 출구 앞 안전정차구역",
            coordinate = Coordinate(37.4948, 127.0632),
            distanceKm = 18,
            durationMinutes = 20,
            reason = PickupReason.ShortWalk(minutes = 1),
        ),
        PlaceModel(
            id = "nearby-2",
            name = "대치동 학원가 갓길 정류장",
            coordinate = Coordinate(37.4951, 127.0578),
            distanceKm = 18,
            durationMinutes = 20,
            reason = PickupReason.NearEntrance,
        ),
        PlaceModel(
            id = "nearby-3",
            name = "은마아파트 후문 안전 지점",
            coordinate = Coordinate(37.4994, 127.0679),
            distanceKm = 18,
            durationMinutes = 20,
            reason = PickupReason.EasyStop,
        ),
    )
}
