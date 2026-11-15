package com.offcourse.picko.core.network.quota.di

import com.offcourse.picko.core.network.quota.KakaoQuotaGuard
import com.offcourse.picko.core.network.quota.KakaoQuotaGuardImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QuotaModule {
    @Binds
    @Singleton
    abstract fun bindKakaoQuotaGuard(impl: KakaoQuotaGuardImpl): KakaoQuotaGuard
}
