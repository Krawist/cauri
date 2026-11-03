package com.cauri.core.data.repository

import com.cauri.core.data.model.Budget
import com.cauri.core.data.model.BudgetSuggestion
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [BudgetRepository] for tests: the test sets the suggestions; budgets set and ignored are kept. */
class FakeBudgetRepository : BudgetRepository {
    /**
     * A budget set by [setBudget].
     *
     * @property categoryId Category.
     * @property amount Amount a month.
     * @property suggested Amount suggested next to it; `null` when typed alone.
     */
    data class Set(val categoryId: Long, val amount: Long, val suggested: Long?)

    override val budgets = MutableStateFlow<List<Budget>>(emptyList())

    /** What [suggestions] answers, without the categories set or ignored since. */
    var suggested = listOf<BudgetSuggestion>()

    val set = mutableListOf<Set>()

    val ignored = mutableListOf<Long>()

    override suspend fun suggestions() = suggested.filter { suggestion ->
        suggestion.categoryId !in ignored && set.none { it.categoryId == suggestion.categoryId }
    }

    override suspend fun setBudget(categoryId: Long, amount: Long, suggested: Long?) {
        set += Set(categoryId = categoryId, amount = amount, suggested = suggested)
    }

    override suspend fun ignore(categoryId: Long) {
        ignored += categoryId
    }
}
