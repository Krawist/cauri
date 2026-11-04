package com.cauri.core.capture.di

import com.cauri.core.capture.battery.BatteryService
import com.cauri.core.capture.battery.BatteryServiceImpl
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.capture.permission.PermissionServiceImpl
import com.cauri.core.capture.place.AddressService
import com.cauri.core.capture.place.AddressServiceImpl
import com.cauri.core.capture.place.GeofenceService
import com.cauri.core.capture.place.GeofenceServiceImpl
import com.cauri.core.capture.place.LocationService
import com.cauri.core.capture.place.LocationServiceImpl
import com.cauri.core.capture.place.PlaceWatcher
import com.cauri.core.capture.place.PlaceWatcherImpl
import com.cauri.core.capture.place.VisitRecorder
import com.cauri.core.capture.place.VisitRecorderImpl
import com.cauri.core.capture.reminder.EveningSummarySender
import com.cauri.core.capture.reminder.EveningSummarySenderImpl
import com.cauri.core.capture.reminder.ReminderDecider
import com.cauri.core.capture.reminder.ReminderDeciderImpl
import com.cauri.core.capture.reminder.ReminderLearner
import com.cauri.core.capture.reminder.ReminderLearnerImpl
import com.cauri.core.capture.reminder.ReminderNotifier
import com.cauri.core.capture.reminder.ReminderNotifierImpl
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
import com.cauri.core.engine.Bandit
import com.cauri.core.engine.BanditImpl
import com.cauri.core.engine.FeatureEncoder
import com.cauri.core.engine.FeatureEncoderImpl
import com.cauri.core.engine.ReminderGuardrails
import com.cauri.core.engine.ReminderGuardrailsImpl
import com.cauri.core.engine.ReminderRules
import com.cauri.core.engine.ReminderRulesImpl
import com.cauri.core.engine.RewardTable
import com.cauri.core.engine.RewardTableImpl
import com.cauri.core.engine.SummaryTime
import com.cauri.core.engine.SummaryTimeImpl
import com.cauri.core.parsing.MessageParser
import com.cauri.core.parsing.MessageParserImpl
import com.cauri.core.parsing.ModelMessageReader
import com.cauri.core.parsing.ModelMessageReaderImpl
import com.cauri.core.parsing.TemplateCatalogReader
import com.cauri.core.parsing.TemplateCatalogReaderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// One binding per component of the module.
@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
internal abstract class CaptureModule {
    @Binds
    abstract fun bindAddressService(impl: AddressServiceImpl): AddressService

    @Binds
    abstract fun bindBatteryService(impl: BatteryServiceImpl): BatteryService

    @Binds
    abstract fun bindDetectionService(impl: DetectionServiceImpl): DetectionService

    @Binds
    abstract fun bindGeofenceService(impl: GeofenceServiceImpl): GeofenceService

    @Binds
    abstract fun bindLocationService(impl: LocationServiceImpl): LocationService

    @Binds
    abstract fun bindMessageParser(impl: MessageParserImpl): MessageParser

    @Binds
    abstract fun bindModelMessageReader(impl: ModelMessageReaderImpl): ModelMessageReader

    @Binds
    abstract fun bindPermissionService(impl: PermissionServiceImpl): PermissionService

    @Binds
    abstract fun bindPlaceWatcher(impl: PlaceWatcherImpl): PlaceWatcher

    @Binds
    abstract fun bindReminderDecider(impl: ReminderDeciderImpl): ReminderDecider

    @Binds
    abstract fun bindReminderNotifier(impl: ReminderNotifierImpl): ReminderNotifier

    @Binds
    abstract fun bindReminderRules(impl: ReminderRulesImpl): ReminderRules

    @Binds
    abstract fun bindReminderGuardrails(impl: ReminderGuardrailsImpl): ReminderGuardrails

    @Binds
    abstract fun bindBandit(impl: BanditImpl): Bandit

    @Binds
    abstract fun bindRewardTable(impl: RewardTableImpl): RewardTable

    @Binds
    abstract fun bindReminderLearner(impl: ReminderLearnerImpl): ReminderLearner

    @Binds
    abstract fun bindSummaryTime(impl: SummaryTimeImpl): SummaryTime

    @Binds
    abstract fun bindEveningSummarySender(impl: EveningSummarySenderImpl): EveningSummarySender

    @Binds
    abstract fun bindFeatureEncoder(impl: FeatureEncoderImpl): FeatureEncoder

    @Binds
    abstract fun bindSignalFilterService(impl: SignalFilterServiceImpl): SignalFilterService

    @Binds
    abstract fun bindSignalProcessor(impl: SignalProcessorImpl): SignalProcessor

    @Binds
    abstract fun bindTemplateCatalogReader(impl: TemplateCatalogReaderImpl): TemplateCatalogReader

    @Binds
    abstract fun bindTemplateCatalogRepository(impl: TemplateCatalogRepositoryImpl): TemplateCatalogRepository

    @Binds
    abstract fun bindVisitRecorder(impl: VisitRecorderImpl): VisitRecorder

    @Binds
    abstract fun bindWatchedSenderRepository(impl: WatchedSenderRepositoryImpl): WatchedSenderRepository
}
