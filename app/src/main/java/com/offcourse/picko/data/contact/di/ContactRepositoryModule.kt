package com.offcourse.picko.data.contact.di

import com.offcourse.picko.data.contact.repository.ContactRepository
import com.offcourse.picko.data.contact.repositoryimpl.ContactRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContactRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository
}
