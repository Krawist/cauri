package com.cauri.feature.cash.reminder

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ReminderModule {
    @Binds
    abstract fun bindCountReminderNotifier(impl: CountReminderNotifierImpl): CountReminderNotifier
}
