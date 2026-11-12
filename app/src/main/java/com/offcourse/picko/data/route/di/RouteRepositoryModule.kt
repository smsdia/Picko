package com.offcourse.picko.data.route.di

import com.offcourse.picko.data.route.repository.RouteRepository
import com.offcourse.picko.data.route.repositoryimpl.FakeRouteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RouteRepositoryModule {
    // 길찾기 연동(카카오모빌리티 길찾기 API 등)이 확정되면 이 바인딩을 실제 구현으로 교체한다.
    @Binds
    @Singleton
    abstract fun bindRouteRepository(impl: FakeRouteRepository): RouteRepository
}
