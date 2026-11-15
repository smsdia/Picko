package com.offcourse.picko.data.place.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.place.model.PickupReason
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.place.repository.PlaceRepository
import javax.inject.Inject

/** 연동하지 못하는 부분(주변 장소 추천, 키가 없을 때의 장소 검색)에 쓰는 가짜 구현. 질의와 위치에 상관없이 시안의 예시 목록을 돌려준다. */
class FakePlaceRepository @Inject constructor() : PlaceRepository {
    override suspend fun searchPlaces(query: String, vehicle: Coordinate): List<PlaceModel> = listOf(
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

    // 서울역 근처의 가짜 추천. 주변 장소 추천 경로는 서버 모델(추천 먼저, 세션 생성 나중)과 맞지 않아 연동 전이다.
    override suspend fun recommendNearby(passenger: Coordinate): List<PlaceModel> = listOf(
        PlaceModel(
            id = "nearby-1",
            name = "서울역 1번 출구 앞 안전정차구역",
            coordinate = Coordinate(37.5550, 126.9724),
            distanceKm = 18,
            durationMinutes = 20,
            reason = PickupReason.ShortWalk(minutes = 1),
        ),
        PlaceModel(
            id = "nearby-2",
            name = "서울역 버스환승센터 갓길 정류장",
            coordinate = Coordinate(37.5558, 126.9713),
            distanceKm = 18,
            durationMinutes = 20,
            reason = PickupReason.NearEntrance,
        ),
        PlaceModel(
            id = "nearby-3",
            name = "서울스퀘어 앞 안전 지점",
            coordinate = Coordinate(37.5565, 126.9738),
            distanceKm = 18,
            durationMinutes = 20,
            reason = PickupReason.EasyStop,
        ),
    )
}
