package com.cauri.core.data.repository

import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.NewFixedCost
import kotlinx.coroutines.flow.Flow

/** The fixed costs of the user, and the expenses paid every month Cauri asks about. */
interface FixedCostRepository {
    /** The fixed costs that hold, by day of the month, each saying whether this month paid it, again after changes. */
    val fixedCosts: Flow<List<FixedCost>>

    /**
     * The expenses paid every month over the last months that are no fixed cost yet and were never refused, again
     * after each change.
     */
    val suggestions: Flow<List<FixedCostSuggestion>>

    /** Saves [cost], typed by the user or a suggestion confirmed. */
    suspend fun add(cost: NewFixedCost)

    /** Replaces the fixed cost [id] by [cost]. */
    suspend fun update(id: Long, cost: NewFixedCost)

    suspend fun delete(id: Long)

    /** Refuses the suggested [cost]: Cauri asks about it no more. */
    suspend fun ignore(cost: NewFixedCost)
}
