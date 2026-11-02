package com.cauri.core.data.repository

import java.time.Instant

/** Counts of the cash in pocket, the starting points of the estimated Cash balance. */
interface CashCountRepository {
    /**
     * Records a count of [amount] (minor units of the Cash account's currency) made at [countedAt]. [expectedAmount]
     * is the estimated balance at that moment, `null` for the first count.
     */
    suspend fun addCount(amount: Long, countedAt: Instant, expectedAmount: Long?)
}
