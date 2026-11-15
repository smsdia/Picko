package com.offcourse.picko.data.pickuplink

import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.localstorage.session.SessionTokenStorage
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.pickuplink.datasource.PickoSessionDataSource
import com.offcourse.picko.data.pickuplink.datasource.PickoSessionSocket
import com.offcourse.picko.data.pickuplink.datasource.SocketEnd
import com.offcourse.picko.data.pickuplink.datasource.SocketState
import com.offcourse.picko.data.pickuplink.dto.CancelSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.CreateSessionRequestDto
import com.offcourse.picko.data.pickuplink.dto.SessionResponseDto
import com.offcourse.picko.data.pickuplink.model.PickoServerException
import com.offcourse.picko.data.pickuplink.model.PickupSessionEndedException
import com.offcourse.picko.data.pickuplink.model.SmsNotSentException
import com.offcourse.picko.data.pickuplink.repositoryimpl.PickupLinkRepositoryImpl
import com.offcourse.picko.data.place.model.PlaceModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PickupLinkRepositoryImplTest {
    private val storage = FakeSessionTokenStorage()
    private val dataSource = FakeDataSource()
    private val socket = FakeSocket()
    private val repository = PickupLinkRepositoryImpl(dataSource, storage, socket)
    private val contact = ContactModel(name = "홍길동", phoneNumber = "010-1234-5678")
    private val pickup = PlaceModel(
        id = "p1",
        name = "CU 합정역점",
        coordinate = Coordinate(37.552, 126.918),
        distanceKm = 2,
        durationMinutes = 5,
    )

    @Test
    fun `세션을 만들고 토큰을 저장하며 전화번호는 숫자만 보낸다`() = runBlocking {
        repository.sendLink(contact, pickup)

        val request = dataSource.created.single()
        assertEquals("01012345678", request.passengerPhone)
        assertEquals(37.552, request.pickupLat, 0.0)
        assertEquals(2_000.0, request.vehicleDistanceM!!, 0.0)
        assertEquals("token-1", storage.token)
    }

    @Test
    fun `문자가 가지 않으면 세션을 바로 닫고 예외를 던진다`() {
        dataSource.smsSent = false

        assertThrows(SmsNotSentException::class.java) { runBlocking { repository.sendLink(contact, pickup) } }
        assertEquals("sms_failed", dataSource.canceled.single().reason)
        assertNull(storage.token)
    }

    @Test
    fun `문자 실패 세션을 닫지 못하면 토큰을 남겨 다음에 정리한다`() {
        dataSource.smsSent = false
        dataSource.cancelError = PickoServerException(code = null, message = "연결 실패")

        assertThrows(SmsNotSentException::class.java) { runBlocking { repository.sendLink(contact, pickup) } }
        assertEquals("token-1", storage.token)
    }

    @Test
    fun `흐름이 끝났는데 세션이 열려 있으면 백그라운드에서 닫는다`() = runBlocking {
        storage.token = "t"

        repository.abandonSession()
        waitUntil { dataSource.canceled.isNotEmpty() && storage.token == null }

        assertEquals("flow_abandoned", dataSource.canceled.single().reason)
        assertNull(storage.token)
    }

    @Test
    fun `세션이 없으면 흐름이 끝나도 서버를 부르지 않는다`() = runBlocking {
        repository.abandonSession()
        Thread.sleep(200)

        assertTrue(dataSource.canceled.isEmpty())
    }

    @Test
    fun `닫는 동안 새 세션 토큰이 저장되면 새 토큰은 지우지 않는다`() = runBlocking {
        storage.token = "old"
        dataSource.onCancel = { storage.token = "new" }

        repository.completeSession()

        assertEquals("new", storage.token)
    }

    @Test
    fun `세션을 만들면 받은 토큰으로 소켓을 연결한다`() = runBlocking {
        repository.sendLink(contact, pickup)

        assertEquals(listOf("token-1"), socket.connected)
    }

    @Test
    fun `문자가 가지 않으면 소켓을 연결하지 않는다`() {
        dataSource.smsSent = false

        assertThrows(SmsNotSentException::class.java) { runBlocking { repository.sendLink(contact, pickup) } }
        assertTrue(socket.connected.isEmpty())
    }

    @Test
    fun `탑승자 위치가 오면 돌려준다`() = runBlocking {
        repository.sendLink(contact, pickup)
        socket.state.value = SocketState(pedestrian = Coordinate(37.5, 127.0))

        assertEquals(Coordinate(37.5, 127.0), repository.awaitPassengerLocation())
    }

    @Test
    fun `기다리는 중에 서버가 세션을 취소하면 예외를 던진다`() {
        runBlocking { repository.sendLink(contact, pickup) }
        socket.state.value = SocketState(end = SocketEnd.SessionCanceled)

        val error = assertThrows(PickupSessionEndedException::class.java) {
            runBlocking { repository.awaitPassengerLocation() }
        }
        assertTrue(error.isCanceled)
    }

    @Test
    fun `탑승자 위치가 바뀔 때마다 내보내고 같은 위치는 건너뛴다`() = runBlocking {
        repository.sendLink(contact, pickup)
        socket.state.value = SocketState(pedestrian = Coordinate(37.5, 127.0))
        assertEquals(Coordinate(37.5, 127.0), repository.observePassengerLocation().first())

        socket.state.value = SocketState(pedestrian = Coordinate(37.5, 127.0), end = null)
        val updates = repository.observePassengerLocation().take(1).toList()
        assertEquals(listOf(Coordinate(37.5, 127.0)), updates)
    }

    @Test
    fun `세션이 없으면 탑승자 위치를 내보내지 않는다`() = runBlocking {
        assertTrue(repository.observePassengerLocation().toList().isEmpty())
    }

    @Test
    fun `차량 위치는 세션이 있을 때만 보낸다`() = runBlocking {
        repository.sendVehicleLocation(Coordinate(1.0, 2.0))
        assertTrue(socket.sent.isEmpty())

        repository.sendLink(contact, pickup)
        repository.sendVehicleLocation(Coordinate(1.0, 2.0))
        assertEquals(listOf(Coordinate(1.0, 2.0)), socket.sent)
    }

    @Test
    fun `취소하면 소켓도 닫는다`() = runBlocking {
        storage.token = "t"

        repository.cancelSession()

        assertTrue(socket.isClosed)
    }

    @Test
    fun `픽업이 끝나면 완료 사유로 서버 세션을 닫고 소켓과 토큰을 정리한다`() = runBlocking {
        storage.token = "t"

        repository.completeSession()

        assertEquals("pickup_completed", dataSource.canceled.single().reason)
        assertTrue(socket.isClosed)
        assertNull(storage.token)
    }

    @Test
    fun `완료 처리 때 서버에 닿지 못하면 토큰을 남긴다`() {
        storage.token = "t"
        dataSource.cancelError = PickoServerException(code = null, message = "연결 실패")

        assertThrows(PickoServerException::class.java) { runBlocking { repository.completeSession() } }
        assertEquals("t", storage.token)
    }

    @Test
    fun `이전 세션이 남아 있으면 새로 만들기 전에 취소한다`() = runBlocking {
        storage.token = "stale"

        repository.sendLink(contact, pickup)

        assertEquals("stale", dataSource.canceled.single().token)
        assertEquals("token-1", storage.token)
    }

    @Test
    fun `남은 세션을 정리하지 못해도 새 세션은 만든다`() = runBlocking {
        storage.token = "stale"
        dataSource.cancelError = PickoServerException(code = null, message = "연결 실패")

        repository.sendLink(contact, pickup)

        assertEquals(1, dataSource.created.size)
        assertEquals("token-1", storage.token)
    }

    @Test
    fun `취소하면 서버에 알리고 토큰을 지운다`() = runBlocking {
        storage.token = "t"

        repository.cancelSession()

        assertEquals("t", dataSource.canceled.single().token)
        assertNull(storage.token)
    }

    @Test
    fun `연결 실패로 취소하지 못하면 토큰을 남긴다`() {
        storage.token = "t"
        dataSource.cancelError = PickoServerException(code = null, message = "연결 실패")

        assertThrows(PickoServerException::class.java) { runBlocking { repository.cancelSession() } }
        assertEquals("t", storage.token)
    }

    @Test
    fun `서버가 이미 끝난 세션이라고 답하면 토큰을 지운다`() = runBlocking {
        storage.token = "t"
        dataSource.cancelError = PickoServerException(code = 404, message = "없음")

        repository.cancelSession()

        assertNull(storage.token)
    }

    @Test
    fun `세션이 없으면 취소는 아무것도 하지 않는다`() = runBlocking {
        repository.cancelSession()

        assertTrue(dataSource.canceled.isEmpty())
    }

    private class FakeSessionTokenStorage : SessionTokenStorage {
        var token: String? = null

        override suspend fun get(): String? = token

        override suspend fun save(token: String) {
            this.token = token
        }

        override suspend fun clear() {
            token = null
        }
    }

    private class FakeSocket : PickoSessionSocket {
        override val state: MutableStateFlow<SocketState> = MutableStateFlow(SocketState())
        val connected = mutableListOf<String>()
        val sent = mutableListOf<Coordinate>()
        var isClosed = false

        override fun connect(token: String) {
            connected += token
        }

        override fun sendVehicleLocation(location: Coordinate): Boolean {
            sent += location
            return true
        }

        override fun close() {
            isClosed = true
        }
    }

    private class FakeDataSource : PickoSessionDataSource {
        val created = mutableListOf<CreateSessionRequestDto>()
        val canceled = mutableListOf<CancelSessionRequestDto>()
        var smsSent = true
        var cancelError: PickoServerException? = null
        var onCancel: () -> Unit = {}

        override suspend fun createSession(request: CreateSessionRequestDto): SessionResponseDto {
            created += request
            return SessionResponseDto(token = "token-${created.size}", status = "CREATED", smsSent = smsSent)
        }

        override suspend fun cancelSession(request: CancelSessionRequestDto) {
            canceled += request
            onCancel()
            cancelError?.let { throw it }
        }
    }

    // 백그라운드 코루틴이 끝나기를 짧게 기다린다.
    private fun waitUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 3_000
        while (!condition() && System.currentTimeMillis() < deadline) Thread.sleep(20)
    }
}
