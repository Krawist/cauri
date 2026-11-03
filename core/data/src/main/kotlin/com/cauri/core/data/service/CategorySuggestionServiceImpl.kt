package com.cauri.core.data.service

import com.cauri.core.data.model.Category
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.min

/** Default implementation for [CategorySuggestionService]. */
@Singleton
internal class CategorySuggestionServiceImpl @Inject constructor() : CategorySuggestionService {
    override fun suggest(
        uses: List<CategoryUse>,
        categories: List<Category>,
        at: Instant,
        zone: ZoneId,
        limit: Int,
    ): List<Category> {
        val minute = minuteOfDay(at, zone)
        val inSlot = uses.filter { minutesApart(minuteOfDay(it.occurredAt, zone), minute) <= SLOT_MINUTES }
            .groupingBy { it.categoryId }.eachCount()
        val anyTime = uses.groupingBy { it.categoryId }.eachCount()
        // sortedWith is stable: categories equally used keep the order given.
        return categories
            .sortedWith(
                compareByDescending<Category> { inSlot[it.id] ?: 0 }.thenByDescending { anyTime[it.id] ?: 0 },
            )
            .take(limit)
    }

    private fun minuteOfDay(at: Instant, zone: ZoneId): Int = at.atZone(zone).toLocalTime().let {
        it.hour * MINUTES_PER_HOUR + it.minute
    }

    // Across midnight too: 23 h 30 and 0 h 30 are an hour apart.
    private fun minutesApart(first: Int, second: Int): Int = abs(first - second).let { min(it, MINUTES_PER_DAY - it) }

    private companion object {
        const val MINUTES_PER_HOUR = 60
        const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
        val SLOT_MINUTES = CategorySuggestionService.SLOT.toMinutes().toInt()
    }
}
