package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.FixedCostDao
import com.cauri.core.data.database.entity.FixedCostEntity
import com.cauri.core.data.database.entity.FixedCostStatus
import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.NewFixedCost
import com.cauri.core.data.service.FixedCostService
import com.cauri.core.data.service.FixedCostSpend
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/** Default implementation for [FixedCostRepository]. */
@Singleton
internal class FixedCostRepositoryImpl @Inject constructor(
    private val dao: FixedCostDao,
    private val service: FixedCostService,
    private val clock: Clock,
) : FixedCostRepository {
    // The months are read as the flows start: the app lives across the first of a month.
    override val fixedCosts: Flow<List<FixedCost>> = flow {
        emitAll(combine(dao.observeAll(), dao.observeExpenses(since = monthStart(monthsBack = 0)), ::withPaid))
    }

    override val suggestions: Flow<List<FixedCostSuggestion>> = flow {
        emitAll(
            combine(
                dao.observeAll(),
                dao.observeExpenses(since = monthStart(monthsBack = FixedCostService.MONTHS - 1)),
                ::unknown,
            ),
        )
    }

    // The fixed costs that hold, each saying whether an expense of the [month] paid it.
    private fun withPaid(all: List<FixedCostEntity>, month: List<FixedCostSpend>) =
        all.filter { it.status == FixedCostStatus.ACTIVE }.map { entity ->
            val cost = entity.toNew()
            FixedCost(
                id = entity.id,
                name = entity.name,
                amount = entity.amount,
                dayOfMonth = entity.dayOfMonth,
                categoryId = entity.categoryId,
                counterparty = entity.counterparty,
                paid = month.any { service.pays(expense = it, cost = cost) },
            )
        }

    // The expenses paid every month, those typed, confirmed or refused before left out.
    private fun unknown(all: List<FixedCostEntity>, expenses: List<FixedCostSpend>): List<FixedCostSuggestion> {
        val known = all.map { it.toNew() }
        return service.suggest(expenses = expenses, zone = clock.zone)
            .filter { suggestion -> known.none { service.same(suggested = suggestion.cost, known = it) } }
            .sortedBy { it.cost.dayOfMonth }
    }

    override suspend fun add(cost: NewFixedCost) {
        dao.insert(cost.toEntity(status = FixedCostStatus.ACTIVE))
    }

    override suspend fun update(id: Long, cost: NewFixedCost) =
        dao.update(cost.toEntity(status = FixedCostStatus.ACTIVE).copy(id = id))

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun ignore(cost: NewFixedCost) {
        dao.insert(cost.toEntity(status = FixedCostStatus.IGNORED))
    }

    // The first instant of the month [monthsBack] months before this one.
    private fun monthStart(monthsBack: Long) =
        YearMonth.now(clock).minusMonths(monthsBack).atDay(1).atStartOfDay(clock.zone).toInstant()

    private fun NewFixedCost.toEntity(status: FixedCostStatus) = FixedCostEntity(
        name = name.trim(),
        amount = amount,
        dayOfMonth = dayOfMonth,
        categoryId = categoryId,
        counterparty = counterparty,
        status = status,
        createdAt = clock.instant(),
    )

    private fun FixedCostEntity.toNew() = NewFixedCost(
        name = name,
        amount = amount,
        dayOfMonth = dayOfMonth,
        categoryId = categoryId,
        counterparty = counterparty,
    )
}
