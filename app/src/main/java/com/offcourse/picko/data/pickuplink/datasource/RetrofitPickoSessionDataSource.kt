package com.offcourse.picko.data.pickuplink.datasource

import com.offcourse.picko.data.pickuplink.dto.CancelSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.CreateSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.SessionResponseDto
import com.offcourse.picko.data.pickuplink.model.PickoServerException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class RetrofitPickoSessionDataSource @Inject constructor(
    private val api: PickoSessionApi,
) : PickoSessionDataSource {
    override suspend fun createSession(request: CreateSessionRequestDto): SessionResponseDto =
        call { api.createSession(request) }

    override suspend fun cancelSession(request: CancelSessionRequestDto) {
        call { api.cancelSession(request) }
    }

    // 요청과 응답에 토큰과 전화번호가 있어 예외 메시지에도 본문을 넣지 않는다.
    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        throw PickoServerException(code = e.code(), message = "서버 응답 오류: HTTP ${e.code()}", cause = e)
    } catch (e: IOException) {
        throw PickoServerException(code = null, message = "서버에 연결하지 못했다", cause = e)
    } catch (e: Exception) {
        // 응답을 읽지 못한 경우 등
        throw PickoServerException(code = null, message = "서버 응답을 처리하지 못했다", cause = e)
    }
}
