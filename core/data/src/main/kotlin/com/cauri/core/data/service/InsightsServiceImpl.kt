package com.cauri.core.data.service

import com.cauri.core.data.model.Budget
import com.cauri.core.data.model.BudgetOverrun
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategorySpending
import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.HabitCost
import com.cauri.core.data.model.Insights
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

/** Default implementation for [InsightsService]. */
@Singleton
internal class InsightsServiceImpl @Inject constructor() : InsightsService {
    override fun insights(
        expenses: List<MonthExpense>,
        categories: List<Category>,
        budgets: List<Budget>,
        today: LocalDate,
    ): Insights {
        val names = categories.associate { it.id to it.name }
        val spentBy = expenses.filter {
            it.categoryId != null
        }.groupBy { it.categoryId }.mapValues { (_, it) -> it.sumOf { e -> e.amount } }
        val budgetBy = budgets.associate { it.categoryId to it.amount }
        val spending = (spentBy.keys + budgetBy.keys).mapNotNull { id ->
            names[id]?.let { CategorySpending(name = it, spent = spentBy[id] ?: 0, budget = budgetBy[id]) }
        }.sortedByDescending { it.spent }
        return Insights(
            spent = expenses.sumOf { it.amount },
            cash = expenses.filter { it.cash }.sumOf { it.amount },
            unidentified = expenses.filter { it.unidentified }.sumOf { it.amount },
            categories = spending,
            overrun = overrun(spending, today),
            habit = null,
        )
    }

    // The category furthest beyond its budget at the end of the month, at the pace of the days gone.
    private fun overrun(spending: List<CategorySpending>, today: LocalDate): BudgetOverrun? {
        val pace = today.lengthOfMonth().toDouble() / today.dayOfMonth
        return spending.mapNotNull { category ->
            val budget = category.budget ?: return@mapNotNull null
            val projected = round(category.spent * pace, STEP)
            BudgetOverrun(
                name = category.name,
                over = (category.spent - budget).coerceAtLeast(0),
                projected = projected,
            )
                .takeIf { projected > budget }
                ?.let { it to projected - budget }
        }.maxByOrNull { it.second }?.first
    }

    // Tapped often enough to tell.
    override fun habitCost(habits: List<Habit>, uses: List<HabitUse>): HabitCost? = habits.mapNotNull { habit ->
        val taps = uses.filter { it.note == habit.name }
        HabitCost(
            name = habit.name,
            uses = taps.size,
            yearly = round(taps.sumOf { it.amount } * DAYS_PER_YEAR / HABIT_DAYS, YEARLY_STEP),
        ).takeIf { taps.size >= MIN_USES }
    }.maxByOrNull { it.yearly }

    private fun round(amount: Double, step: Long): Long = (amount / step).roundToLong() * step

    private companion object {
        const val STEP = 500L
        const val YEARLY_STEP = 1_000L
        const val DAYS_PER_YEAR = 365.0
        const val HABIT_DAYS = 30
        const val MIN_USES = 4
    }
}
