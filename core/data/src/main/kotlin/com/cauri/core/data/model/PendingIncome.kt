package com.cauri.core.data.model

import java.time.Instant

/**
 * An income arrived that Cauri offers to split.
 *
 * @property transactionId The income.
 * @property amount Amount received, in minor units of XAF.
 * @property accountName Account it arrived on (« Orange Money »).
 * @property counterparty Who sent it, as the message names them; `null` when unknown.
 * @property occurredAt When it arrived.
 */
data class PendingIncome(
    val transactionId: Long,
    val amount: Long,
    val accountName: String,
    val counterparty: String?,
    val occurredAt: Instant,
)
