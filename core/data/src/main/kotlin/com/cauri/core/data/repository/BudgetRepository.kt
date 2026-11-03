package com.cauri.core.data.repository

import com.cauri.core.data.model.Budget
import com.cauri.core.data.model.BudgetSuggestion
import kotlinx.coroutines.flow.Flow

/** The monthly budgets of the expense categories, and those Cauri suggests from real spending. */
interface BudgetRepository {
    /** The budgets that hold, by name of category, again after each change. */
    val budgets: Flow<List<Budget>>

    /**
     * The budgets Cauri suggests, by name of category, after four weeks of history: one for each expense category
     * spent on twice at least, without a budget and never ignored.
     */
    suspend fun suggestions(): List<BudgetSuggestion>

    /** Sets the budget of [categoryId] to [amount] a month (minor units), next to the [suggested] one; `null` alone. */
    suspend fun setBudget(categoryId: Long, amount: Long, suggested: Long?)

    /** Ignores the suggestion for [categoryId]: Cauri suggests none again for it. */
    suspend fun ignore(categoryId: Long)
}
