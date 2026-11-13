package com.offcourse.picko.data.navi.datasource

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.data.TripInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber

/** NaviHelper 가 콜백으로 알리는 것 중 앱이 쓰는 것 */
sealed interface NaviEvent {
    data class Trip(val info: TripInfo) : NaviEvent

    data class Error(val code: NaviErrorCode) : NaviEvent
}

/**
 * NaviHelper 객체 하나를 앱이 사는 동안 연결해 두고 콜백을 [events]로 내보낸다.
 * 연결 전에 부른 요청은 [awaitHelper]가 연결될 때까지 기다리고, 연결에 실패하면 null 을 돌려준다.
 * 연결이 한 번 실패하면 다시 시도하지 않는다. 내비 서비스가 없는 환경에서 요청마다 기다리지 않기 위해서다.
 */
@Singleton
class NaviHelperConnection @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _events = MutableSharedFlow<NaviEvent>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<NaviEvent> = _events.asSharedFlow()

    private val ready = CompletableDeferred<NaviHelper?>()
    private var isStarted = false

    private val listener = object : NaviHelperEventListener {
        override fun onTripInfo(tripInfo: TripInfo) {
            _events.tryEmit(NaviEvent.Trip(tripInfo))
        }

        override fun onError(errorCode: NaviErrorCode) {
            Timber.w("NaviHelper error: %s", errorCode)
            _events.tryEmit(NaviEvent.Error(errorCode))
        }
    }

    /** 연결된 NaviHelper. 처음 부를 때 연결을 시작한다. 연결하지 못했으면 null. */
    suspend fun awaitHelper(): NaviHelper? {
        start()
        return ready.await()
    }

    @Synchronized
    private fun start() {
        if (isStarted) return
        isStarted = true

        val helper = NaviHelper(context)
        helper.initialize(
            onConnected = {
                helper.addListener(listener)
                ready.complete(helper)
            },
            onFailed = { throwable ->
                Timber.w(throwable, "NaviHelper 연결에 실패했다")
                ready.complete(null)
            },
        )
    }

    private companion object {
        // 콜백은 앱 바깥 스레드에서 오므로 느린 수집자가 있어도 막히지 않게 둔다.
        const val EVENT_BUFFER = 16
    }
}
