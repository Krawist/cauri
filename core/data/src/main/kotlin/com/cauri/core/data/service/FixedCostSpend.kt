package com.cauri.core.data.service

import java.time.Instant

/**
 * An expense, as the fixed costs are found and recognised from.
 *
 * @property categoryId Its category; `null` when none was chosen.
 * @property categoryName The name of that category; `null` without category.
 * @property counterparty Merchant the message names; `null` when none does.
 * @property note Note of the user; `null` when there is none.
 * @property amount Amount, in minor units of XAF.
 * @property occurredAt When it happened.
 */
internal data class FixedCostSpend(
    val categoryId: Long?,
    val categoryName: String?,
    val counterparty: String?,
    val note: String?,
    val amount: Long,
    val occurredAt: Instant,
)
