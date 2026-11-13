package com.offcourse.picko.data.vehicle.di

import com.offcourse.picko.BuildConfig
import com.offcourse.picko.data.vehicle.repository.VehicleRepository
import com.offcourse.picko.data.vehicle.repositoryimpl.DemoVehicleRepository
import com.offcourse.picko.data.vehicle.repositoryimpl.FusedLocationVehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VehicleRepositoryModule {
    // 디버그 빌드에서 demo.drive 가 켜지면 정해 둔 경로를 따라가는 시연 차량을 쓴다. 릴리스에서는 켜지지 않는다.
    @Provides
    @Singleton
    fun provideVehicleRepository(
        fused: Provider<FusedLocationVehicleRepository>,
        demo: Provider<DemoVehicleRepository>,
    ): VehicleRepository = if (BuildConfig.DEBUG && BuildConfig.DEMO_DRIVE) demo.get() else fused.get()
}
