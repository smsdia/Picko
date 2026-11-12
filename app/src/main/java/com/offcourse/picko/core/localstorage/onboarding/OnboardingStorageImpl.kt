package com.offcourse.picko.core.localstorage.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.offcourse.picko.core.localstorage.qualifier.AppDataStore
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber

private val KeyOnboardingShown = booleanPreferencesKey("onboarding_shown")

class OnboardingStorageImpl @Inject constructor(
    @param:AppDataStore private val dataStore: DataStore<Preferences>,
) : OnboardingStorage {
    override val isFirstLaunch: Flow<Boolean> = dataStore.data
        .catch { error ->
            // 읽기 실패는 처음으로 보고 시작 안내를 다시 보여 준다. 그 밖의 오류는 그대로 올린다.
            if (error !is IOException) throw error

            Timber.w(error, "온보딩 값을 읽지 못했다")
            emit(emptyPreferences())
        }
        .map { preferences -> preferences[KeyOnboardingShown] != true }

    override suspend fun markOnboardingShown() {
        try {
            dataStore.edit { preferences -> preferences[KeyOnboardingShown] = true }
        } catch (e: IOException) {
            Timber.w(e, "온보딩 값을 저장하지 못했다")
        }
    }
}
