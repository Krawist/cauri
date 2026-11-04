package com.cauri.core.data.repository

import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.NewFixedCost
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [FixedCostRepository] for tests: the test sets the fixed costs and the suggestions; changes are kept. */
class FakeFixedCostRepository : FixedCostRepository {
    override val fixedCosts = MutableStateFlow<List<FixedCost>>(emptyList())

    override val suggestions = MutableStateFlow<List<FixedCostSuggestion>>(emptyList())

    val added = mutableListOf<NewFixedCost>()

    val updated = mutableListOf<Pair<Long, NewFixedCost>>()

    val deleted = mutableListOf<Long>()

    val ignored = mutableListOf<NewFixedCost>()

    override suspend fun add(cost: NewFixedCost) {
        added += cost
        suggestions.value = suggestions.value.filter { it.cost != cost }
    }

    override suspend fun update(id: Long, cost: NewFixedCost) {
        updated += id to cost
    }

    override suspend fun delete(id: Long) {
        deleted += id
        fixedCosts.value = fixedCosts.value.filter { it.id != id }
    }

    override suspend fun ignore(cost: NewFixedCost) {
        ignored += cost
        suggestions.value = suggestions.value.filter { it.cost != cost }
    }
}
