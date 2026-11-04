package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.AvailableDao
import com.cauri.core.data.database.dao.GoalDao
import com.cauri.core.data.database.dao.GoalRow
import com.cauri.core.data.database.entity.FixedCostEntity
import com.cauri.core.data.database.entity.FixedCostStatus
import com.cauri.core.data.database.entity.GoalContributionEntity
import com.cauri.core.data.database.entity.GoalEntity
import com.cauri.core.data.model.Goal
import com.cauri.core.data.model.GoalKind
import com.cauri.core.data.model.GoalProgress
import com.cauri.core.data.model.GoalsOverview
import com.cauri.core.data.model.IncomeSplitSettings
import com.cauri.core.data.model.NewGoal
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.service.GoalService
import com.cauri.core.data.service.GoalTotal
import com.cauri.core.data.service.IncomeAt
import java.time.Clock
import java.time.Duration
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/** Default implementation for [GoalRepository]. */
// One function per change of a goal, and the conversions between the goals and their rows.
@Suppress("TooManyFunctions")
@Singleton
internal class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao,
    private val available: AvailableDao,
    private val preferences: UserPreferencesRepository,
    private val service: GoalService,
    private val clock: Clock,
) : GoalRepository {
    // The month is read as the flow starts: the app lives across the first of a month.
    override val overview: Flow<GoalsOverview> = flow {
        val month = YearMonth.now(clock)
        val since = month.atDay(1).atStartOfDay(clock.zone).toInstant()
        val incomesSince = month.minusMonths(GoalService.MONTHS).atDay(1).atStartOfDay(clock.zone).toInstant()
        val figures = combine(
            dao.observeSaved(),
            dao.observeEnvelopesSpent(since),
            dao.observeIncomes(incomesSince),
            preferences.incomeSplit,
            ::Figures,
        )
        emitAll(
            combine(dao.observeAll(), dao.observePutAsideSince(since), figures) { rows, putAside, figures ->
                GoalsOverview(savedThisMonth = putAside, goals = progress(rows, figures, month))
            },
        )
    }

    override suspend fun add(goal: NewGoal) {
        val entity = goal.toEntity(position = dao.getNextPosition(), fixedCostId = null)
        if (goal.kind == GoalKind.TONTINE) dao.insertTontine(entity, goal.toFixedCost()) else dao.insert(entity)
    }

    override suspend fun update(id: Long, goal: NewGoal) {
        val current = dao.get(id) ?: return
        val costId = current.fixedCostId
        if (current.kind == GoalKind.TONTINE && costId != null) {
            dao.updateFixedCost(goal.toFixedCost().copy(id = costId))
        }
        dao.update(goal.toEntity(position = current.position, fixedCostId = costId).copy(id = id, kind = current.kind))
    }

    override suspend fun delete(id: Long) {
        dao.get(id)?.let { dao.deleteWithFixedCost(it) }
    }

    override suspend fun moveFirst(id: Long) = dao.moveFirst(id)

    override suspend fun putAside(id: Long, amount: Long) = dao.insertContribution(
        GoalContributionEntity(goalId = id, amount = amount, at = clock.instant(), transactionId = null),
    )

    override suspend fun createEmergencyFund(name: String) {
        if (preferences.emergencyFundCreated.first()) return
        val spent = available.observeSpent(since = clock.instant() - MONTH).first()
        val amount = if (spent > 0) (spent + FUND_STEP - 1) / FUND_STEP * FUND_STEP else DEFAULT_FUND
        val fund = NewGoal(name = name, kind = GoalKind.SAVING, amount = amount)
        // Before the others: the emergencies come first.
        dao.moveFirst(dao.insert(fund.toEntity(position = 0, fixedCostId = null)))
        preferences.setEmergencyFundCreated()
    }

    // Each goal with what it holds or used, the savings with when they should be reached.
    private fun progress(rows: List<GoalRow>, figures: Figures, month: YearMonth): List<GoalProgress> {
        val saved = figures.saved.associate { it.goalId to it.amount }
        val spent = figures.envelopes.associate { it.goalId to it.amount }
        val savings = rows.filter { it.goal.kind == GoalKind.SAVING }
        val monthly = service.monthlySavings(
            incomes = figures.incomes,
            savingsRate = figures.settings.savingsRate,
            now = month,
            zone = clock.zone,
        )
        val left = savings.map { (it.goal.amount - saved[it.goal.id].orZero()).coerceAtLeast(0) }
        val estimates = savings.map { it.goal.id }.zip(service.estimates(left, monthly, month)).toMap()
        val receiving = savings.zip(left).firstOrNull { (_, amount) -> amount > 0 }?.first?.goal?.id
        return rows.map { row ->
            val goal = row.goal
            GoalProgress(
                goal = row.toGoal(),
                saved = when (goal.kind) {
                    GoalKind.SAVING -> saved[goal.id].orZero()
                    GoalKind.ENVELOPE -> spent[goal.id].orZero()
                    GoalKind.TONTINE -> 0
                },
                estimate = estimates[goal.id],
                receiving = goal.id == receiving,
            )
        }
    }

    private fun Long?.orZero() = this ?: 0

    private fun NewGoal.toEntity(position: Int, fixedCostId: Long?) = GoalEntity(
        name = name.trim(),
        kind = kind,
        amount = amount,
        deadline = deadline.takeIf { kind == GoalKind.SAVING },
        payoutDate = payoutDate.takeIf { kind == GoalKind.TONTINE },
        categoryId = categoryId.takeIf { kind == GoalKind.ENVELOPE },
        fixedCostId = fixedCostId,
        position = position,
        createdAt = clock.instant(),
    )

    private fun NewGoal.toFixedCost() = FixedCostEntity(
        name = name.trim(),
        amount = amount,
        dayOfMonth = dayOfMonth ?: 1,
        categoryId = null,
        counterparty = null,
        status = FixedCostStatus.ACTIVE,
        createdAt = clock.instant(),
    )

    private fun GoalRow.toGoal() = Goal(
        id = goal.id,
        name = goal.name,
        kind = goal.kind,
        amount = goal.amount,
        deadline = goal.deadline,
        payoutDate = goal.payoutDate,
        dayOfMonth = dayOfMonth,
        categoryId = goal.categoryId,
    )

    // What the goals are read with, beside the goals themselves.
    private data class Figures(
        val saved: List<GoalTotal>,
        val envelopes: List<GoalTotal>,
        val incomes: List<IncomeAt>,
        val settings: IncomeSplitSettings,
    )

    private companion object {
        val MONTH: Duration = Duration.ofDays(30)
        const val FUND_STEP = 5_000L
        const val DEFAULT_FUND = 50_000L
    }
}
