package com.offcourse.picko.core.localstorage.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.offcourse.picko.core.localstorage.onboarding.OnboardingStorage
import com.offcourse.picko.core.localstorage.onboarding.OnboardingStorageImpl
import com.offcourse.picko.core.localstorage.qualifier.AppDataStore
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

    companion object {
        private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "picko_app_datastore")

        @AppDataStore
        @Provides
        @Singleton
        fun provideAppDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.appDataStore
    }
}
