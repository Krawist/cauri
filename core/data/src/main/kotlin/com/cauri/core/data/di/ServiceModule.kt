package com.cauri.core.data.di

import com.cauri.core.data.service.CashBalanceService
import com.cauri.core.data.service.CashBalanceServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ServiceModule {
    @Binds
    abstract fun bindCashBalanceService(impl: CashBalanceServiceImpl): CashBalanceService
}
