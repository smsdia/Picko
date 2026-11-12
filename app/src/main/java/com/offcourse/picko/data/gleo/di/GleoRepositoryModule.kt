package com.offcourse.picko.data.gleo.di

import com.offcourse.picko.data.gleo.repository.GleoRepository
import com.offcourse.picko.data.gleo.repositoryimpl.GleoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GleoRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGleoRepository(impl: GleoRepositoryImpl): GleoRepository
}
