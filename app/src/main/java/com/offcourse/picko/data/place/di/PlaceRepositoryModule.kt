package com.offcourse.picko.data.place.di

import com.offcourse.picko.data.place.repository.PlaceRepository
import com.offcourse.picko.data.place.repositoryimpl.FakePlaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaceRepositoryModule {
    // 장소 검색 연동이 확정되면 이 바인딩을 실제 구현으로 교체한다.
    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: FakePlaceRepository): PlaceRepository
}
