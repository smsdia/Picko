package com.offcourse.picko.data.speech.di

import com.offcourse.picko.data.speech.repository.SpeechSynthesisRepository
import com.offcourse.picko.data.speech.repositoryimpl.PleosSpeechSynthesisRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechSynthesisModule {
    @Binds
    @Singleton
    abstract fun bindSpeechSynthesisRepository(impl: PleosSpeechSynthesisRepository): SpeechSynthesisRepository
}
