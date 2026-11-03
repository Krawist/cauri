package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.InsightsDao
import com.cauri.core.data.model.Insights
import com.cauri.core.data.service.InsightsService
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Default implementation for [InsightsRepository]. */
// Injected: the transactions, the categories, the budgets, the habits, the figures and the clock.
@Suppress("LongParameterList")
@Singleton
internal class InsightsRepositoryImpl @Inject constructor(
    private val insightsDao: InsightsDao,
    private val categories: CategoryRepository,
    private val budgets: BudgetRepository,
    private val habits: HabitRepository,
    private val service: InsightsService,
    private val clock: Clock,
) : InsightsRepository {
    override suspend fun insights(): Insights {
        val today = LocalDate.now(clock)
        val from = today.withDayOfMonth(1).atStartOfDay(clock.zone).toInstant()
        val to = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(clock.zone).toInstant()
        val habitList = habits.habits.first()
        val uses = insightsDao.getHabitUses(names = habitList.map { it.name }, since = clock.instant() - HABIT_WINDOW)
        return service.insights(
            expenses = insightsDao.getExpenses(from = from, to = to),
            categories = categories.categories.first(),
            budgets = budgets.budgets.first(),
            today = today,
        ).copy(habit = service.habitCost(habits = habitList, uses = uses))
    }

    private companion object {
        // The pace of the habits: the last 30 days.
        val HABIT_WINDOW: Duration = Duration.ofDays(30)
    }
}
