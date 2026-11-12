package com.offcourse.picko.data.speech.di

import com.offcourse.picko.BuildConfig
import com.offcourse.picko.data.speech.datasource.FakeSpeechTextSource
import com.offcourse.picko.data.speech.datasource.PleosSpeechTextSource
import com.offcourse.picko.data.speech.datasource.SpeechTextSource
import com.offcourse.picko.data.speech.repository.SpeechRecognitionRepository
import com.offcourse.picko.data.speech.repositoryimpl.SpeechRecognitionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechRecognitionModule {
    @Binds
    @Singleton
    abstract fun bindSpeechRecognitionRepository(impl: SpeechRecognitionRepositoryImpl): SpeechRecognitionRepository

    companion object {
        /** 클라이언트 ID/시크릿이 없으면 빌드는 통과시키되 가짜 엔진을 쓴다. */
        @Provides
        @Singleton
        fun provideSpeechTextSource(
            pleos: Provider<PleosSpeechTextSource>,
            fake: Provider<FakeSpeechTextSource>,
        ): SpeechTextSource =
            if (BuildConfig.PLEOS_CLIENT_ID.isBlank() || BuildConfig.PLEOS_CLIENT_SECRET.isBlank()) {
                fake.get()
            } else {
                pleos.get()
            }
    }
}
