package com.cauri.feature.onboarding

import com.cauri.core.data.repository.CashCountRepository
import java.time.Instant

/** In-memory [CashCountRepository] for tests. */
internal class FakeCashCountRepository : CashCountRepository {
    /**
     * A count recorded by [addCount].
     *
     * @property amount Amount counted, in minor units.
     * @property countedAt When the count was made.
     * @property expectedAmount Estimated balance at that moment; `null` for the first count.
     */
    data class Count(val amount: Long, val countedAt: Instant, val expectedAmount: Long?)

    val counts = mutableListOf<Count>()

    override suspend fun addCount(amount: Long, countedAt: Instant, expectedAmount: Long?) {
        counts += Count(amount = amount, countedAt = countedAt, expectedAmount = expectedAmount)
    }
}
