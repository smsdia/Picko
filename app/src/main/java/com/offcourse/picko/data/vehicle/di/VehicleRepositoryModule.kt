package com.offcourse.picko.data.vehicle.di

import com.offcourse.picko.data.vehicle.repository.VehicleRepository
import com.offcourse.picko.data.vehicle.repositoryimpl.FakeVehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VehicleRepositoryModule {
    // Pleos SDK 구현체가 준비되면 이 바인딩을 실제 구현으로 교체한다.
    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: FakeVehicleRepository): VehicleRepository
}
