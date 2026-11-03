package com.cauri.core.data.service

import com.cauri.core.data.model.Category
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** Picks the categories the user most likely wants for a new entry on an account, at a time of day. */
internal interface CategorySuggestionService {
    /**
     * Up to [limit] of [categories], most likely first: the most used in [uses] within two hours of the time of day of
     * [at] in [zone], then the most used at any hour, then the others in the order given. Ties keep that order.
     */
    fun suggest(
        uses: List<CategoryUse>,
        categories: List<Category>,
        at: Instant,
        zone: ZoneId,
        limit: Int,
    ): List<Category>

    companion object {
        /** How far back the uses are read. */
        val HISTORY: Duration = Duration.ofDays(90)

        /** Gap with the time of day of the entry within which a use counts first. */
        val SLOT: Duration = Duration.ofHours(2)
    }
}
