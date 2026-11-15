package com.offcourse.picko.data.speech.di

import com.offcourse.picko.data.speech.datasource.PleosSpeechTextSource
import com.offcourse.picko.data.speech.datasource.SpeechTextSource
import com.offcourse.picko.data.speech.repository.SpeechRecognitionRepository
import com.offcourse.picko.data.speech.repositoryimpl.SpeechRecognitionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechRecognitionModule {
    @Binds
    @Singleton
    abstract fun bindSpeechRecognitionRepository(impl: SpeechRecognitionRepositoryImpl): SpeechRecognitionRepository

    @Binds
    @Singleton
    abstract fun bindSpeechTextSource(impl: PleosSpeechTextSource): SpeechTextSource
}
