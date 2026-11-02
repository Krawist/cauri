package com.cauri.core.capture.di

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
    abstract fun bindPermissionService(impl: PermissionServiceImpl): PermissionService
}
