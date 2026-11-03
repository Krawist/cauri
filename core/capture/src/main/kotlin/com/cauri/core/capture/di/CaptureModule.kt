package com.cauri.core.capture.di

import com.cauri.core.capture.battery.BatteryService
import com.cauri.core.capture.battery.BatteryServiceImpl
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.capture.permission.PermissionServiceImpl
import com.cauri.core.capture.signal.DetectionService
import com.cauri.core.capture.signal.DetectionServiceImpl
import com.cauri.core.capture.signal.SignalFilterService
import com.cauri.core.capture.signal.SignalFilterServiceImpl
import com.cauri.core.capture.signal.SignalProcessor
import com.cauri.core.capture.signal.SignalProcessorImpl
import com.cauri.core.capture.signal.TemplateCatalogRepository
import com.cauri.core.capture.signal.TemplateCatalogRepositoryImpl
import com.cauri.core.capture.signal.WatchedSenderRepository
import com.cauri.core.capture.signal.WatchedSenderRepositoryImpl
import com.cauri.core.parsing.MessageParser
import com.cauri.core.parsing.MessageParserImpl
import com.cauri.core.parsing.TemplateCatalogReader
import com.cauri.core.parsing.TemplateCatalogReaderImpl
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
    abstract fun bindDetectionService(impl: DetectionServiceImpl): DetectionService

    @Binds
    abstract fun bindMessageParser(impl: MessageParserImpl): MessageParser

    @Binds
    abstract fun bindPermissionService(impl: PermissionServiceImpl): PermissionService

    @Binds
    abstract fun bindSignalFilterService(impl: SignalFilterServiceImpl): SignalFilterService

    @Binds
    abstract fun bindSignalProcessor(impl: SignalProcessorImpl): SignalProcessor

    @Binds
    abstract fun bindTemplateCatalogReader(impl: TemplateCatalogReaderImpl): TemplateCatalogReader

    @Binds
    abstract fun bindTemplateCatalogRepository(impl: TemplateCatalogRepositoryImpl): TemplateCatalogRepository

    @Binds
    abstract fun bindWatchedSenderRepository(impl: WatchedSenderRepositoryImpl): WatchedSenderRepository
}
