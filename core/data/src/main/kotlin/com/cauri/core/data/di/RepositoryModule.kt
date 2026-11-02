package com.cauri.core.data.di

import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.preferences.UserPreferencesRepositoryImpl
import com.cauri.core.data.repository.AccountRepository
import com.cauri.core.data.repository.AccountRepositoryImpl
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.CashCountRepositoryImpl
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.CategoryRepositoryImpl
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.data.repository.TransactionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    abstract fun bindCashCountRepository(impl: CashCountRepositoryImpl): CashCountRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
}
