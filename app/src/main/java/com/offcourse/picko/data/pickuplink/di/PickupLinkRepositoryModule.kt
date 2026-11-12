package com.offcourse.picko.data.pickuplink.di

import com.offcourse.picko.data.pickuplink.repository.PickupLinkRepository
import com.offcourse.picko.data.pickuplink.repositoryimpl.FakePickupLinkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PickupLinkRepositoryModule {
    // 링크 전달 방식과 탑승자 웹 페이지가 확정되면 이 바인딩을 실제 구현으로 교체한다.
    @Binds
    @Singleton
    abstract fun bindPickupLinkRepository(impl: FakePickupLinkRepository): PickupLinkRepository
}
