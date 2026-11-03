package com.cauri.core.data.service

import java.time.Instant

/**
 * An expense of a category: what the budgets are suggested from.
 *
 * @property categoryId Category of the expense.
 * @property amount Amount, in minor units of XAF.
 * @property occurredAt When it happened.
 */
internal data class CategorySpend(val categoryId: Long, val amount: Long, val occurredAt: Instant)
