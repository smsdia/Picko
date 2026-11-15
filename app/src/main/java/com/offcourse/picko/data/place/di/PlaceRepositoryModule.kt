package com.offcourse.picko.data.place.di

import com.offcourse.picko.core.network.di.KakaoLocal
import com.offcourse.picko.core.network.di.PickoServer
import com.offcourse.picko.data.place.datasource.KakaoLocalApi
import com.offcourse.picko.data.place.datasource.PickupRecommendApi
import com.offcourse.picko.data.place.repository.PickupRecommendRepository
import com.offcourse.picko.data.place.repository.PlaceRepository
import com.offcourse.picko.data.place.repositoryimpl.KakaoPlaceRepository
import com.offcourse.picko.data.place.repositoryimpl.PickupRecommendRepositoryImpl
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
    // 목적지 찾기는 카카오 로컬, 픽업 후보 추천은 Picko 서버다.
    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: KakaoPlaceRepository): PlaceRepository

    @Binds
    @Singleton
    abstract fun bindPickupRecommendRepository(impl: PickupRecommendRepositoryImpl): PickupRecommendRepository

    companion object {
        @Provides
        @Singleton
        fun provideKakaoLocalApi(@KakaoLocal retrofit: Retrofit): KakaoLocalApi =
            retrofit.create(KakaoLocalApi::class.java)

        @Provides
        @Singleton
        fun providePickupRecommendApi(@PickoServer retrofit: Retrofit): PickupRecommendApi =
            retrofit.create(PickupRecommendApi::class.java)
    }
}
