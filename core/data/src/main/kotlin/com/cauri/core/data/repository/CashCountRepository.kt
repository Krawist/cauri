package com.cauri.core.data.repository

import com.cauri.core.data.model.CashBalance
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** Counts of the cash in pocket, the starting points of the estimated Cash balance. */
interface CashCountRepository {
    /**
     * The estimated cash in pocket, again after each count and each cash transaction; `null` before the first count.
     */
    val balance: Flow<CashBalance?>

    /**
     * Records a count of [amount] (minor units of the Cash account's currency) made at [countedAt]. [expectedAmount]
     * is the estimated balance at that moment, `null` for the first count. Counting more than expected records the
     * surplus as found money, an income on the Cash account at the time of the count, which the count already holds.
     */
    suspend fun addCount(amount: Long, countedAt: Instant, expectedAmount: Long?)
}
