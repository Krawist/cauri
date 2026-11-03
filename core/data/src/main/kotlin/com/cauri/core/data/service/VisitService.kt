package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.VisitEntity
import java.time.Duration
import java.time.Instant

/** The rules of the visits to shops: what was paid during them, and the amount their card proposes. */
internal interface VisitService {
    /** Whether a transaction made at [at] belongs to [visit]: from its entry to 15 minutes after its exit. */
    fun covers(visit: VisitEntity, at: Instant): Boolean

    /** The usual amount of [expenses], the oldest first: their median, the lower one when two are in the middle. */
    fun usualAmount(expenses: List<TransactionEntity>): Long?

    companion object {
        /** How long after the exit a message still belongs to the visit: an SMS may come late. */
        val AFTER_EXIT: Duration = Duration.ofMinutes(15)
    }
}
