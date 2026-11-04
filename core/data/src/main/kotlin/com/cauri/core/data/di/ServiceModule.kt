package com.cauri.core.data.di

import com.cauri.core.data.service.ActivityQueryService
import com.cauri.core.data.service.ActivityQueryServiceImpl
import com.cauri.core.data.service.ActivityService
import com.cauri.core.data.service.ActivityServiceImpl
import com.cauri.core.data.service.AvailableService
import com.cauri.core.data.service.AvailableServiceImpl
import com.cauri.core.data.service.BudgetService
import com.cauri.core.data.service.BudgetServiceImpl
import com.cauri.core.data.service.CashBalanceService
import com.cauri.core.data.service.CashBalanceServiceImpl
import com.cauri.core.data.service.CategoryPredictionService
import com.cauri.core.data.service.CategoryPredictionServiceImpl
import com.cauri.core.data.service.CategorySuggestionService
import com.cauri.core.data.service.CategorySuggestionServiceImpl
import com.cauri.core.data.service.CountReminderService
import com.cauri.core.data.service.CountReminderServiceImpl
import com.cauri.core.data.service.DiscoveryService
import com.cauri.core.data.service.DiscoveryServiceImpl
import com.cauri.core.data.service.DuplicateService
import com.cauri.core.data.service.DuplicateServiceImpl
import com.cauri.core.data.service.FixedCostService
import com.cauri.core.data.service.FixedCostServiceImpl
import com.cauri.core.data.service.GoalService
import com.cauri.core.data.service.GoalServiceImpl
import com.cauri.core.data.service.IncomeSplitService
import com.cauri.core.data.service.IncomeSplitServiceImpl
import com.cauri.core.data.service.InsightsService
import com.cauri.core.data.service.InsightsServiceImpl
import com.cauri.core.data.service.OutcomeService
import com.cauri.core.data.service.OutcomeServiceImpl
import com.cauri.core.data.service.PlaceService
import com.cauri.core.data.service.PlaceServiceImpl
import com.cauri.core.data.service.ProposalService
import com.cauri.core.data.service.ProposalServiceImpl
import com.cauri.core.data.service.TransferLinkService
import com.cauri.core.data.service.TransferLinkServiceImpl
import com.cauri.core.data.service.VisitService
import com.cauri.core.data.service.VisitServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// One binding per service of the module.
@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
internal abstract class ServiceModule {
    @Binds
    abstract fun bindInsightsService(impl: InsightsServiceImpl): InsightsService

    @Binds
    abstract fun bindBudgetService(impl: BudgetServiceImpl): BudgetService

    @Binds
    abstract fun bindAvailableService(impl: AvailableServiceImpl): AvailableService

    @Binds
    abstract fun bindFixedCostService(impl: FixedCostServiceImpl): FixedCostService

    @Binds
    abstract fun bindGoalService(impl: GoalServiceImpl): GoalService

    @Binds
    abstract fun bindIncomeSplitService(impl: IncomeSplitServiceImpl): IncomeSplitService

    @Binds
    abstract fun bindCategoryPredictionService(impl: CategoryPredictionServiceImpl): CategoryPredictionService

    @Binds
    abstract fun bindActivityQueryService(impl: ActivityQueryServiceImpl): ActivityQueryService

    @Binds
    abstract fun bindActivityService(impl: ActivityServiceImpl): ActivityService

    @Binds
    abstract fun bindCashBalanceService(impl: CashBalanceServiceImpl): CashBalanceService

    @Binds
    abstract fun bindCategorySuggestionService(impl: CategorySuggestionServiceImpl): CategorySuggestionService

    @Binds
    abstract fun bindDiscoveryService(impl: DiscoveryServiceImpl): DiscoveryService

    @Binds
    abstract fun bindDuplicateService(impl: DuplicateServiceImpl): DuplicateService

    @Binds
    abstract fun bindCountReminderService(impl: CountReminderServiceImpl): CountReminderService

    @Binds
    abstract fun bindTransferLinkService(impl: TransferLinkServiceImpl): TransferLinkService

    @Binds
    abstract fun bindPlaceService(impl: PlaceServiceImpl): PlaceService

    @Binds
    abstract fun bindOutcomeService(impl: OutcomeServiceImpl): OutcomeService

    @Binds
    abstract fun bindVisitService(impl: VisitServiceImpl): VisitService

    @Binds
    abstract fun bindProposalService(impl: ProposalServiceImpl): ProposalService
}
