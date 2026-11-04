package com.cauri.core.data.di

import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.preferences.UserPreferencesRepositoryImpl
import com.cauri.core.data.repository.AccountRepository
import com.cauri.core.data.repository.AccountRepositoryImpl
import com.cauri.core.data.repository.ActivityRepository
import com.cauri.core.data.repository.ActivityRepositoryImpl
import com.cauri.core.data.repository.AvailableRepository
import com.cauri.core.data.repository.AvailableRepositoryImpl
import com.cauri.core.data.repository.BanditRepository
import com.cauri.core.data.repository.BanditRepositoryImpl
import com.cauri.core.data.repository.BudgetRepository
import com.cauri.core.data.repository.BudgetRepositoryImpl
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.CashCountRepositoryImpl
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.CategoryRepositoryImpl
import com.cauri.core.data.repository.ConfirmationRepository
import com.cauri.core.data.repository.ConfirmationRepositoryImpl
import com.cauri.core.data.repository.DecisionLogRepository
import com.cauri.core.data.repository.DecisionLogRepositoryImpl
import com.cauri.core.data.repository.DiscoveryRepository
import com.cauri.core.data.repository.DiscoveryRepositoryImpl
import com.cauri.core.data.repository.FixedCostRepository
import com.cauri.core.data.repository.FixedCostRepositoryImpl
import com.cauri.core.data.repository.HabitRepository
import com.cauri.core.data.repository.HabitRepositoryImpl
import com.cauri.core.data.repository.IncomeSplitRepository
import com.cauri.core.data.repository.IncomeSplitRepositoryImpl
import com.cauri.core.data.repository.InsightsRepository
import com.cauri.core.data.repository.InsightsRepositoryImpl
import com.cauri.core.data.repository.PlaceRepository
import com.cauri.core.data.repository.PlaceRepositoryImpl
import com.cauri.core.data.repository.ProposalRepository
import com.cauri.core.data.repository.ProposalRepositoryImpl
import com.cauri.core.data.repository.RawSignalRepository
import com.cauri.core.data.repository.RawSignalRepositoryImpl
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.data.repository.TransactionRepositoryImpl
import com.cauri.core.data.repository.TransferRepository
import com.cauri.core.data.repository.TransferRepositoryImpl
import com.cauri.core.data.repository.VisitRepository
import com.cauri.core.data.repository.VisitRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// One binding per repository of the module.
@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindInsightsRepository(impl: InsightsRepositoryImpl): InsightsRepository

    @Binds
    abstract fun bindAvailableRepository(impl: AvailableRepositoryImpl): AvailableRepository

    @Binds
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    abstract fun bindFixedCostRepository(impl: FixedCostRepositoryImpl): FixedCostRepository

    @Binds
    abstract fun bindIncomeSplitRepository(impl: IncomeSplitRepositoryImpl): IncomeSplitRepository

    @Binds
    abstract fun bindBanditRepository(impl: BanditRepositoryImpl): BanditRepository

    @Binds
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    abstract fun bindCashCountRepository(impl: CashCountRepositoryImpl): CashCountRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindDecisionLogRepository(impl: DecisionLogRepositoryImpl): DecisionLogRepository

    @Binds
    abstract fun bindDiscoveryRepository(impl: DiscoveryRepositoryImpl): DiscoveryRepository

    @Binds
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    abstract fun bindPlaceRepository(impl: PlaceRepositoryImpl): PlaceRepository

    @Binds
    abstract fun bindRawSignalRepository(impl: RawSignalRepositoryImpl): RawSignalRepository

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository

    @Binds
    abstract fun bindProposalRepository(impl: ProposalRepositoryImpl): ProposalRepository

    @Binds
    abstract fun bindConfirmationRepository(impl: ConfirmationRepositoryImpl): ConfirmationRepository

    @Binds
    abstract fun bindVisitRepository(impl: VisitRepositoryImpl): VisitRepository
}
