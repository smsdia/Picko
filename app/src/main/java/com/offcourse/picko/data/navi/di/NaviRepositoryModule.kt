package com.offcourse.picko.data.navi.di

import com.offcourse.picko.BuildConfig
import com.offcourse.picko.data.navi.repository.NaviRepository
import com.offcourse.picko.data.navi.repositoryimpl.DemoNaviRepository
import com.offcourse.picko.data.navi.repositoryimpl.NaviHelperRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NaviRepositoryModule {
    // 시연 차량을 쓸 때는 그 위치로 시간을 계산하는 시연용 내비를 쓴다(VehicleRepositoryModule 과 같은 조건).
    @Provides
    @Singleton
    fun provideNaviRepository(
        real: Provider<NaviHelperRepository>,
        demo: Provider<DemoNaviRepository>,
    ): NaviRepository = if (BuildConfig.DEBUG && BuildConfig.DEMO_DRIVE) demo.get() else real.get()
}
