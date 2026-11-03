package com.cauri.core.data.di

import com.cauri.core.data.service.CashBalanceService
import com.cauri.core.data.service.CashBalanceServiceImpl
import com.cauri.core.data.service.CountReminderService
import com.cauri.core.data.service.CountReminderServiceImpl
import com.cauri.core.data.service.DuplicateService
import com.cauri.core.data.service.DuplicateServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ServiceModule {
    @Binds
    abstract fun bindCashBalanceService(impl: CashBalanceServiceImpl): CashBalanceService

    @Binds
    abstract fun bindDuplicateService(impl: DuplicateServiceImpl): DuplicateService

    @Binds
    abstract fun bindCountReminderService(impl: CountReminderServiceImpl): CountReminderService
}
