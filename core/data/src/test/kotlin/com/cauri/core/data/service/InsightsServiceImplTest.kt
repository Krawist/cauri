package com.cauri.core.data.service

import com.cauri.core.data.model.Budget
import com.cauri.core.data.model.BudgetOverrun
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.CategorySpending
import com.cauri.core.data.model.Habit
import com.cauri.core.data.model.HabitCost
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class InsightsServiceImplTest {
    private val service = InsightsServiceImpl()

    @Nested
    inner class Insights {
        @Test
        fun `should total the month, its cash and the rest of the counts`() {
            // given
            val expenses = listOf(
                MonthExpense(categoryId = 1, amount = 10_000, cash = true, unidentified = false),
                MonthExpense(categoryId = null, amount = 4_100, cash = true, unidentified = true),
                MonthExpense(categoryId = 2, amount = 6_000, cash = false, unidentified = false),
            )

            // when
            val insights = insights(expenses = expenses)

            // then
            Triple(insights.spent, insights.cash, insights.unidentified) shouldBe Triple(20_100L, 14_100L, 4_100L)
        }

        @Test
        fun `should give each category against its budget, the costliest first`() {
            // given
            val expenses = listOf(
                MonthExpense(categoryId = 1, amount = 10_000, cash = true, unidentified = false),
                MonthExpense(categoryId = 2, amount = 31_500, cash = false, unidentified = false),
            )

            // when
            val insights =
                insights(
                    expenses = expenses,
                    budgets = listOf(Budget(2, "Transport", 28_000), Budget(3, "Loisirs", 15_000)),
                )

            // then
            insights.categories shouldContainExactly listOf(
                CategorySpending(name = "Transport", spent = 31_500, budget = 28_000),
                CategorySpending(name = "Alimentation", spent = 10_000, budget = null),
                CategorySpending(name = "Loisirs", spent = 0, budget = 15_000),
            )
        }

        @Test
        fun `should project the overrun at the pace of the days gone`() {
            // given
            val expenses = listOf(MonthExpense(categoryId = 2, amount = 31_500, cash = false, unidentified = false))

            // when
            val insights = insights(
                expenses = expenses,
                budgets = listOf(Budget(2, "Transport", 28_000)),
                today = LocalDate.of(2026, 11, 18),
            )

            // then
            insights.overrun shouldBe BudgetOverrun(name = "Transport", over = 3_500, projected = 52_500)
        }
    }

    @Nested
    inner class HabitCostOf {
        @Test
        fun `should tell the yearly cost of the habit tapped the most`() {
            // given
            val uses =
                List(20) { HabitUse(note = "Beignets", amount = 500) } +
                    List(3) { HabitUse(note = "Moto", amount = 1_000) }

            // when
            val cost = service.habitCost(
                habits = listOf(
                    Habit(id = 1, name = "Beignets", amount = 500, categoryId = 1),
                    Habit(id = 2, name = "Moto", amount = 1_000, categoryId = 2),
                ),
                uses = uses,
            )

            // then
            cost shouldBe HabitCost(name = "Beignets", uses = 20, yearly = 122_000)
        }
    }

    private fun insights(
        expenses: List<MonthExpense> = emptyList(),
        budgets: List<Budget> = emptyList(),
        today: LocalDate = LocalDate.of(2026, 11, 15),
    ) = service.insights(
        expenses = expenses,
        categories = listOf(
            Category(id = 1, name = "Alimentation", nature = CategoryNature.EXPENSE),
            Category(id = 2, name = "Transport", nature = CategoryNature.EXPENSE),
            Category(id = 3, name = "Loisirs", nature = CategoryNature.EXPENSE),
        ),
        budgets = budgets,
        today = today,
    )
}
