package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.AvailableDao
import com.cauri.core.data.database.dao.IncomeSplitDao
import com.cauri.core.data.service.AvailableService
import com.cauri.core.data.service.MonthMoney
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/** Default implementation for [AvailableRepository]. */
@Singleton
internal class AvailableRepositoryImpl @Inject constructor(
    private val dao: AvailableDao,
    private val splits: IncomeSplitDao,
    private val fixedCosts: FixedCostRepository,
    private val service: AvailableService,
    private val clock: Clock,
) : AvailableRepository {
    // The month is read as the flow starts: the app lives across the first of a month.
    override val today: Flow<Long?> = flow {
        val since = YearMonth.now(clock).atDay(1).atStartOfDay(clock.zone).toInstant()
        emitAll(
            combine(
                dao.observeReceived(since),
                dao.observeSpent(since),
                splits.observeSplits(since),
                fixedCosts.fixedCosts,
            ) { received, spent, split, costs ->
                received.takeIf { it > 0 }?.let {
                    val month = MonthMoney(
                        received = it,
                        savings = split.sumOf { income -> income.savings },
                        unpaidFixedCosts = costs.filterNot { cost -> cost.paid }.sumOf { cost -> cost.amount },
                        spent = spent,
                    )
                    service.perDay(month, today = LocalDate.now(clock))
                }
            },
        )
    }
}
