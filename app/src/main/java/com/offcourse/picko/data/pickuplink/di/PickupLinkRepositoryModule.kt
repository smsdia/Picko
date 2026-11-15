package com.offcourse.picko.data.pickuplink.di

import com.offcourse.picko.core.network.di.PickoServer
import com.offcourse.picko.data.pickuplink.datasource.PickoSessionApi
import com.offcourse.picko.data.pickuplink.datasource.PickoSessionDataSource
import com.offcourse.picko.data.pickuplink.datasource.PickoSessionSocket
import com.offcourse.picko.data.pickuplink.datasource.PickoVehicleSocket
import com.offcourse.picko.data.pickuplink.datasource.RetrofitPickoSessionDataSource
import com.offcourse.picko.data.pickuplink.repository.PickupLinkRepository
import com.offcourse.picko.data.pickuplink.repositoryimpl.PickupLinkRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class PickupLinkRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPickupLinkRepository(impl: PickupLinkRepositoryImpl): PickupLinkRepository

    @Binds
    @Singleton
    abstract fun bindPickoSessionDataSource(impl: RetrofitPickoSessionDataSource): PickoSessionDataSource

    @Binds
    @Singleton
    abstract fun bindPickoSessionSocket(impl: PickoVehicleSocket): PickoSessionSocket

    companion object {
        @Provides
        @Singleton
        fun providePickoSessionApi(@PickoServer retrofit: Retrofit): PickoSessionApi =
            retrofit.create(PickoSessionApi::class.java)
    }
}
