package com.cauri.core.capture.di

import com.cauri.core.capture.health.CaptureHealthNotifier
import com.cauri.core.capture.health.CaptureHealthNotifierImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** The warning that the capture receives no more messages; its rule is bound per build type. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class HealthModule {
    @Binds
    abstract fun bindCaptureHealthNotifier(impl: CaptureHealthNotifierImpl): CaptureHealthNotifier
}
