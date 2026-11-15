package com.offcourse.picko.core.localstorage.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.offcourse.picko.core.crypto.AadContext
import com.offcourse.picko.core.crypto.CryptoException
import com.offcourse.picko.core.crypto.CryptoManager
import com.offcourse.picko.core.localstorage.qualifier.SessionDataStore
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

private val KeySessionToken = stringPreferencesKey("session_token")

class SessionTokenStorageImpl @Inject constructor(
    @param:SessionDataStore private val dataStore: DataStore<Preferences>,
    private val cryptoManager: CryptoManager,
) : SessionTokenStorage {
    // 암호화 저장이 안 되는 기기에서는 이 값이 세션 동안의 유일한 보관처다.
    @Volatile
    private var cached: String? = null

    override suspend fun get(): String? {
        cached?.let { return it }

        val encrypted = try {
            dataStore.data.map { it[KeySessionToken] }.first()
        } catch (e: IOException) {
            Timber.w(e, "세션 토큰을 읽지 못했다")
            return null
        } ?: return null

        return try {
            cryptoManager.decrypt(encrypted, AadContext.SessionToken).also { cached = it }
        } catch (e: CryptoException) {
            // 복구할 수 없는 값은 남겨 두지 않는다.
            Timber.w(e, "저장된 세션 토큰을 복호화하지 못해 지운다")
            clear()
            null
        }
    }

    override suspend fun save(token: String) {
        cached = token

        try {
            val encrypted = cryptoManager.encrypt(token, AadContext.SessionToken)
            dataStore.edit { it[KeySessionToken] = encrypted }
        } catch (e: CryptoException) {
            Timber.w(e, "세션 토큰을 암호화하지 못해 메모리에만 둔다")
            removeFromDisk()
        } catch (e: IOException) {
            Timber.w(e, "세션 토큰을 저장하지 못해 메모리에만 둔다")
            removeFromDisk()
        }
    }

    override suspend fun clear() {
        cached = null
        removeFromDisk()
    }

    // 저장에 실패하면 디스크에 남은 이전 토큰이 낡은 값이 되지 않게 함께 지운다.
    private suspend fun removeFromDisk() {
        try {
            dataStore.edit { it.remove(KeySessionToken) }
        } catch (e: IOException) {
            Timber.w(e, "세션 토큰을 지우지 못했다")
        }
    }
}
