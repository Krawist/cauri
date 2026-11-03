package com.cauri.core.capture.signal

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class JournalModule {
    @Binds
    abstract fun bindNotificationJournal(impl: LogNotificationJournal): NotificationJournal
}
