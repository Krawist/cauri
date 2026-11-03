package com.cauri.core.data.model

import java.time.Instant

/**
 * A visit to a shop whose card, a cash expense to confirm, still waits for the user.
 *
 * @property transactionId Expense to confirm the visit created.
 * @property placeName Name of the place (« Marché Mokolo »).
 * @property categoryId Category of the expense, the place's default one until the user picks another; `null` for
 *   none.
 * @property enteredAt When the user came in.
 * @property leftAt When the user left.
 * @property proposedAmount Usual amount spent there, in minor units; `null` before the first expense there.
 */
data class PendingVisit(
    val transactionId: Long,
    val placeName: String,
    val categoryId: Long?,
    val enteredAt: Instant,
    val leftAt: Instant,
    val proposedAmount: Long?,
)
