package com.cauri.core.capture.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
internal object ReminderModule {
    /** The draws of the reminders bandit: injected, so that tests can fix them. */
    @Provides
    fun provideRandom(): Random = Random.Default
}
