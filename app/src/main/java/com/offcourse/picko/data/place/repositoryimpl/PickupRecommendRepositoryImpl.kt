package com.offcourse.picko.data.place.repositoryimpl

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.data.place.datasource.PickupRecommendApi
import com.offcourse.picko.data.place.dto.PickupRecommendDto
import com.offcourse.picko.data.place.model.PickupRecommendException
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.place.repository.PickupRecommendRepository
import java.io.IOException
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

private const val RECOMMEND_LIMIT = 3
private const val METERS_PER_KM = 1_000.0

class PickupRecommendRepositoryImpl @Inject constructor(
    private val api: PickupRecommendApi,
) : PickupRecommendRepository {
    override suspend fun recommend(vehicle: Coordinate, destination: Coordinate): List<PlaceModel> {
        val response = try {
            api.recommend(
                vehicleLat = vehicle.latitude,
                vehicleLng = vehicle.longitude,
                destinationLat = destination.latitude,
                destinationLng = destination.longitude,
                limit = RECOMMEND_LIMIT,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            // 요청에 위치가 있어 메시지에 본문과 주소를 넣지 않는다.
            throw PickupRecommendException("서버 응답 오류: HTTP ${e.code()}", e)
        } catch (e: IOException) {
            throw PickupRecommendException("서버에 연결하지 못했다", e)
        } catch (e: Exception) {
            throw PickupRecommendException("서버 응답을 처리하지 못했다", e)
        }

        return response.recommendations.mapIndexed { index, item -> item.toPlace(index) }
    }
}

internal fun PickupRecommendDto.Item.toPlace(index: Int): PlaceModel = PlaceModel(
    // 서버가 후보 ID를 주지 않아 좌표로 구분한다. 같은 지점이 두 번 오지 않으므로 목록의 키로 쓸 수 있다.
    id = "recommend-$index-$lat-$lng",
    name = label,
    coordinate = Coordinate(latitude = lat, longitude = lng),
    distanceKm = (vehicleDistanceM / METERS_PER_KM).roundToInt(),
    durationMinutes = ceil(vehicleEtaMin).toInt().coerceAtLeast(1),
)
