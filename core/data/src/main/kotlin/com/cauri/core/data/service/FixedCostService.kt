package com.cauri.core.data.service

import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.NewFixedCost
import java.time.ZoneId

/** The rules of the fixed costs: which expenses come back every month, and which expense pays a fixed cost. */
internal interface FixedCostService {
    /**
     * The expenses of [expenses] paid every month: one payment a month in [MIN_MONTHS] calendar months at least, the
     * amounts within 10 % of their median; grouped by merchant, else by category and amount. Each comes with the
     * median amount and the day of its latest payment in [zone].
     */
    fun suggest(expenses: List<FixedCostSpend>, zone: ZoneId): List<FixedCostSuggestion>

    /**
     * Whether [expense] pays [cost]: the same merchant, its name as the merchant, the note or the category of the
     * expense, or the same category for about the same amount (10 %). Case, accents and spaces around are ignored.
     */
    fun pays(expense: FixedCostSpend, cost: NewFixedCost): Boolean

    /** Whether [suggested] is [known] already: the same merchant, or the same category for about the same amount. */
    fun same(suggested: NewFixedCost, known: NewFixedCost): Boolean

    companion object {
        /** Calendar months read for the suggestions: the current one and the three before. */
        const val MONTHS = 4L

        /** Months with a payment before Cauri asks. */
        const val MIN_MONTHS = 2
    }
}
