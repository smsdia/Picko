package com.offcourse.picko.core.network.quota

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.offcourse.picko.core.localstorage.qualifier.AppDataStore
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

// 카카오 쿼터는 한국 시간 자정에 초기화된다.
private val QuotaZone: ZoneId = ZoneId.of("Asia/Seoul")
private val KeyDate = stringPreferencesKey("kakao_quota_date")

private fun countKey(api: KakaoApi) = intPreferencesKey("kakao_quota_${api.name}")

@Singleton
class KakaoQuotaGuardImpl @Inject constructor(
    @param:AppDataStore private val dataStore: DataStore<Preferences>,
) : KakaoQuotaGuard {
    private val clock: Clock = Clock.system(QuotaZone)
    private val mutex = Mutex()

    override suspend fun hasRoom(api: KakaoApi, count: Int): Boolean = mutex.withLock {
        usedToday(api) + count <= api.dailyLimit
    }

    override suspend fun tryConsume(api: KakaoApi, count: Int): Boolean = mutex.withLock {
        val used = usedToday(api)
        if (used + count > api.dailyLimit) {
            Timber.w("카카오 API 하루 한도에 닿아 호출하지 않는다: %s", api.name)
            return@withLock false
        }

        try {
            dataStore.edit { preferences ->
                resetIfNewDay(preferences)
                preferences[countKey(api)] = used + count
            }
        } catch (e: IOException) {
            // 세지 못해도 흐름은 막지 않는다. 다음 호출에서 다시 센다.
            Timber.w(e, "카카오 호출 수를 저장하지 못했다")
        }
        true
    }

    private suspend fun usedToday(api: KakaoApi): Int {
        val preferences = try {
            dataStore.data.first()
        } catch (e: IOException) {
            Timber.w(e, "카카오 호출 수를 읽지 못했다")
            return 0
        }

        return if (preferences[KeyDate] == today()) preferences[countKey(api)] ?: 0 else 0
    }

    private fun resetIfNewDay(preferences: androidx.datastore.preferences.core.MutablePreferences) {
        val today = today()
        if (preferences[KeyDate] == today) return

        KakaoApi.entries.forEach { preferences.remove(countKey(it)) }
        preferences[KeyDate] = today
    }

    private fun today(): String = LocalDate.now(clock).toString()
}
