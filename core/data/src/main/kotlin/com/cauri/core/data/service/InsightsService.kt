package com.cauri.core.data.service

import com.cauri.core.data.model.Budget
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.HabitCost
import com.cauri.core.data.model.Insights
import java.time.LocalDate

/** The figures of « Analyses »: computed here, never by a language model, which may only word them. */
internal interface InsightsService {
    /**
     * The insights of the month of [today] from its [expenses]: the total and its cash part, the categories against
     * their [budgets] and the overrun at the pace of the days gone; no habit, [habitCost] tells it.
     */
    fun insights(
        expenses: List<MonthExpense>,
        categories: List<Category>,
        budgets: List<Budget>,
        today: LocalDate,
    ): Insights

    /** The habit of [habits] that costs the most over a year at the pace of its [uses] over the last 30 days. */
    fun habitCost(habits: List<Habit>, uses: List<HabitUse>): HabitCost?
}
