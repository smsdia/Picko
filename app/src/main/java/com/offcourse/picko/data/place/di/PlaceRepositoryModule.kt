package com.offcourse.picko.data.place.di

import com.offcourse.picko.core.network.di.KakaoLocal
import com.offcourse.picko.data.place.datasource.KakaoLocalApi
import com.offcourse.picko.data.place.repository.PlaceRepository
import com.offcourse.picko.data.place.repositoryimpl.KakaoPlaceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaceRepositoryModule {
    // 장소 검색은 카카오 로컬, 주변 장소 추천은 아직 가짜 구현이다(KakaoPlaceRepository 참조).
    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: KakaoPlaceRepository): PlaceRepository

    companion object {
        @Provides
        @Singleton
        fun provideKakaoLocalApi(@KakaoLocal retrofit: Retrofit): KakaoLocalApi =
            retrofit.create(KakaoLocalApi::class.java)
    }
}
