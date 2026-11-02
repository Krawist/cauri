package com.cauri.core.data.model

import java.time.Instant

/**
 * Estimated cash in pocket, always shown with « ≈ »: the last count, moved by the cash transactions since.
 *
 * @property amount Estimated balance, in minor units of the Cash account's currency; negative when more cash was spent
 * than counted and received, until the next count corrects it.
 * @property countedAt When the last count was made.
 * @property expensesSinceCount Number of cash expenses recorded since that count.
 */
data class CashBalance(val amount: Long, val countedAt: Instant, val expensesSinceCount: Int)
