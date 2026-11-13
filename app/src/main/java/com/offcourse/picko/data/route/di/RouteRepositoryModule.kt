package com.offcourse.picko.data.route.di

import com.offcourse.picko.data.route.repository.RouteRepository
import com.offcourse.picko.data.route.repositoryimpl.KakaoRouteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RouteRepositoryModule {
    // 차량 경로는 카카오모빌리티 길찾기, 도보와 목적지는 아직 가짜 구현이다(KakaoRouteRepository 참조).
    @Binds
    @Singleton
    abstract fun bindRouteRepository(impl: KakaoRouteRepository): RouteRepository
}
