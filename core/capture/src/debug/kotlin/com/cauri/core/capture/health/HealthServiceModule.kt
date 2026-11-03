package com.cauri.core.capture.health

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class HealthServiceModule {
    @Binds
    abstract fun bindCaptureHealthService(impl: DebugCaptureHealthService): CaptureHealthService
}
