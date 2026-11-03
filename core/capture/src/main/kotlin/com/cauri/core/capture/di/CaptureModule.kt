package com.cauri.core.capture.di

import com.cauri.core.capture.battery.BatteryService
import com.cauri.core.capture.battery.BatteryServiceImpl
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.capture.permission.PermissionServiceImpl
import com.cauri.core.capture.signal.SignalFilterService
import com.cauri.core.capture.signal.SignalFilterServiceImpl
import com.cauri.core.capture.signal.WatchedSenderRepository
import com.cauri.core.capture.signal.WatchedSenderRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CaptureModule {
    @Binds
    abstract fun bindBatteryService(impl: BatteryServiceImpl): BatteryService

    @Binds
    abstract fun bindPermissionService(impl: PermissionServiceImpl): PermissionService

    @Binds
    abstract fun bindSignalFilterService(impl: SignalFilterServiceImpl): SignalFilterService

    @Binds
    abstract fun bindWatchedSenderRepository(impl: WatchedSenderRepositoryImpl): WatchedSenderRepository
}
