package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.VisitEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [VisitService]. */
@Singleton
internal class VisitServiceImpl @Inject constructor() : VisitService {
    override fun covers(visit: VisitEntity, at: Instant): Boolean =
        !at.isBefore(visit.enteredAt) && visit.leftAt?.let { !at.isAfter(it + VisitService.AFTER_EXIT) } != false

    override fun usualAmount(expenses: List<TransactionEntity>): Long? {
        if (expenses.isEmpty()) return null
        val sorted = expenses.map { it.amount }.sorted()
        // The lower of the two middle ones when they are even: an amount actually paid.
        return sorted[(sorted.size - 1) / 2]
    }
}
