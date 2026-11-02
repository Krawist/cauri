package com.cauri.core.data.repository

import com.cauri.core.data.model.CashBalance
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [CashCountRepository] for tests. */
class FakeCashCountRepository : CashCountRepository {
    /**
     * A count recorded by [addCount].
     *
     * @property amount Amount counted, in minor units.
     * @property countedAt When the count was made.
     * @property expectedAmount Estimated balance at that moment; `null` for the first count.
     */
    data class Count(val amount: Long, val countedAt: Instant, val expectedAmount: Long?)

    val counts = mutableListOf<Count>()

    override val balance = MutableStateFlow<CashBalance?>(null)

    override suspend fun addCount(amount: Long, countedAt: Instant, expectedAmount: Long?) {
        counts += Count(amount = amount, countedAt = countedAt, expectedAmount = expectedAmount)
    }
}
