package com.cauri.core.data.service

import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/** Default implementation for [CategoryPredictionService]. */
@Singleton
internal class CategoryPredictionServiceImpl @Inject constructor() : CategoryPredictionService {
    override fun predict(uses: List<CategoryUse>, now: Instant): List<Long> {
        val halfLife = CategoryPredictionService.HALF_LIFE.toMillis().toDouble()
        return uses.groupBy { it.categoryId }
            .map { (category, chosen) ->
                val weight = chosen.sumOf { HALF.pow(Duration.between(it.occurredAt, now).toMillis() / halfLife) }
                Triple(category, weight, chosen.maxOf { it.occurredAt })
            }
            .sortedWith(compareByDescending<Triple<Long, Double, Instant>> { it.second }.thenByDescending { it.third })
            .map { it.first }
    }

    private companion object {
        const val HALF = 0.5
    }
}
