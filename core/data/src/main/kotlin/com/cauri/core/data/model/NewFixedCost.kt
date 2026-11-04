package com.cauri.core.data.model

/**
 * A fixed cost to save, typed by the user or found by Cauri.
 *
 * @property name What the user calls it, or the merchant it was found from.
 * @property amount Amount a month, in minor units of XAF.
 * @property dayOfMonth Day of the month it is usually paid, from 1 to 31.
 * @property categoryId Category of its payments; `null` when not known.
 * @property counterparty Merchant its payments name in the messages; `null` when none does.
 */
data class NewFixedCost(
    val name: String,
    val amount: Long,
    val dayOfMonth: Int,
    val categoryId: Long? = null,
    val counterparty: String? = null,
)
