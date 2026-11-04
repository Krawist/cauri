package com.cauri.core.data.service

import java.time.Instant

/**
 * An income, as the savings to come are estimated from.
 *
 * @property amount In minor units of XAF.
 * @property occurredAt When it arrived.
 */
internal data class IncomeAt(val amount: Long, val occurredAt: Instant)
