package com.offcourse.picko.core.localstorage.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.offcourse.picko.core.localstorage.onboarding.OnboardingStorage
import com.offcourse.picko.core.localstorage.onboarding.OnboardingStorageImpl
import com.offcourse.picko.core.localstorage.qualifier.AppDataStore
import com.offcourse.picko.core.localstorage.qualifier.SessionDataStore
import com.offcourse.picko.core.localstorage.session.SessionTokenStorage
import com.offcourse.picko.core.localstorage.session.SessionTokenStorageImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalStorageModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingStorage(impl: OnboardingStorageImpl): OnboardingStorage

    @Binds
    @Singleton
    abstract fun bindSessionTokenStorage(impl: SessionTokenStorageImpl): SessionTokenStorage

    companion object {
        private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "picko_app_datastore")

        @AppDataStore
        @Provides
        @Singleton
        fun provideAppDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.appDataStore

        // 파일 이름은 백업 제외 규칙(backup_rules.xml, data_extraction_rules.xml)과 같아야 한다.
        private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "picko_session_datastore")

        @SessionDataStore
        @Provides
        @Singleton
        fun provideSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.sessionDataStore
    }
}
