package com.cauri.widget

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WidgetModule {
    @Binds
    abstract fun bindCashWidgetUpdater(impl: CashWidgetUpdaterImpl): CashWidgetUpdater
}
