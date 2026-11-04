package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.GoalDao
import com.cauri.core.data.database.dao.IncomeSplitDao
import com.cauri.core.data.database.entity.IncomeSplitEntity
import com.cauri.core.data.model.IncomeSplit
import com.cauri.core.data.model.IncomeSplitProposal
import com.cauri.core.data.model.PendingIncome
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.service.FixedCostTotals
import com.cauri.core.data.service.IncomeSplitService
import java.time.Clock
import java.time.Duration
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/** Default implementation for [IncomeSplitRepository]. */
@Singleton
internal class IncomeSplitRepositoryImpl @Inject constructor(
    private val dao: IncomeSplitDao,
    private val goals: GoalDao,
    private val fixedCosts: FixedCostRepository,
    private val preferences: UserPreferencesRepository,
    private val service: IncomeSplitService,
    private val clock: Clock,
) : IncomeSplitRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val pending: Flow<PendingIncome?> = preferences.incomeSplit.flatMapLatest { settings ->
        dao.observePending(threshold = settings.threshold, since = clock.instant() - RECENT)
    }.map { it.firstOrNull() }

    override suspend fun proposal(income: PendingIncome, savingsRate: Int): IncomeSplitProposal {
        val (paid, unpaid) = fixedCosts.fixedCosts.first().partition { it.paid }
        val monthStart = YearMonth.now(clock).atDay(1).atStartOfDay(clock.zone).toInstant()
        val totals = FixedCostTotals(
            unpaid = unpaid.sumOf { it.amount },
            paid = paid.sumOf { it.amount },
            kept = dao.observeSplits(since = monthStart).first().sumOf { it.fixedCosts },
        )
        return IncomeSplitProposal(
            income = income,
            savingsRate = savingsRate,
            split = service.propose(amount = income.amount, savingsRate = savingsRate, fixedCosts = totals),
            fixedCosts = unpaid,
        )
    }

    // The savings go to the first saving not reached (CAU-44).
    override suspend fun split(transactionId: Long, split: IncomeSplit) = goals.split(
        IncomeSplitEntity(
            transactionId = transactionId,
            savings = split.savings,
            fixedCosts = split.fixedCosts,
            budgets = split.budgets,
            dismissed = false,
            splitAt = clock.instant(),
        ),
    )

    override suspend fun dismiss(transactionId: Long) = dao.insert(
        IncomeSplitEntity(
            transactionId = transactionId,
            savings = 0,
            fixedCosts = 0,
            budgets = 0,
            dismissed = true,
            splitAt = clock.instant(),
        ),
    )

    private companion object {
        // An older income, such as those received before Cauri was installed, is spent already.
        val RECENT: Duration = Duration.ofDays(7)
    }
}
