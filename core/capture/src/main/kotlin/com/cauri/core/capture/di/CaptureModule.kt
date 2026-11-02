package com.cauri.core.capture.di

import com.cauri.core.capture.battery.BatteryService
import com.cauri.core.capture.battery.BatteryServiceImpl
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.capture.permission.PermissionServiceImpl
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
}
