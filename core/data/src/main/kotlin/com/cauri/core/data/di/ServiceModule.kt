package com.cauri.core.data.di

import com.cauri.core.data.service.ActivityQueryService
import com.cauri.core.data.service.ActivityQueryServiceImpl
import com.cauri.core.data.service.ActivityService
import com.cauri.core.data.service.ActivityServiceImpl
import com.cauri.core.data.service.CashBalanceService
import com.cauri.core.data.service.CashBalanceServiceImpl
import com.cauri.core.data.service.CategorySuggestionService
import com.cauri.core.data.service.CategorySuggestionServiceImpl
import com.cauri.core.data.service.CountReminderService
import com.cauri.core.data.service.CountReminderServiceImpl
import com.cauri.core.data.service.DuplicateService
import com.cauri.core.data.service.DuplicateServiceImpl
import com.cauri.core.data.service.PlaceService
import com.cauri.core.data.service.PlaceServiceImpl
import com.cauri.core.data.service.ProposalService
import com.cauri.core.data.service.ProposalServiceImpl
import com.cauri.core.data.service.TransferLinkService
import com.cauri.core.data.service.TransferLinkServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ServiceModule {
    @Binds
    abstract fun bindActivityQueryService(impl: ActivityQueryServiceImpl): ActivityQueryService

    @Binds
    abstract fun bindActivityService(impl: ActivityServiceImpl): ActivityService

    @Binds
    abstract fun bindCashBalanceService(impl: CashBalanceServiceImpl): CashBalanceService

    @Binds
    abstract fun bindCategorySuggestionService(impl: CategorySuggestionServiceImpl): CategorySuggestionService

    @Binds
    abstract fun bindDuplicateService(impl: DuplicateServiceImpl): DuplicateService

    @Binds
    abstract fun bindCountReminderService(impl: CountReminderServiceImpl): CountReminderService

    @Binds
    abstract fun bindTransferLinkService(impl: TransferLinkServiceImpl): TransferLinkService

    @Binds
    abstract fun bindPlaceService(impl: PlaceServiceImpl): PlaceService

    @Binds
    abstract fun bindProposalService(impl: ProposalServiceImpl): ProposalService
}
