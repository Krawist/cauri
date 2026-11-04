package com.cauri.core.data.model

/**
 * A cost that comes back every month, such as the rent or the electricity.
 *
 * @property id Identifier.
 * @property name What the user calls it, or the merchant it was found from.
 * @property amount Amount a month, in minor units of XAF.
 * @property dayOfMonth Day of the month it is usually paid, from 1 to 31.
 * @property categoryId Category of its payments; `null` when not known.
 * @property counterparty Merchant its payments name in the messages; `null` when none does.
 * @property paid Whether an expense of this month paid it already.
 */
data class FixedCost(
    val id: Long,
    val name: String,
    val amount: Long,
    val dayOfMonth: Int,
    val categoryId: Long?,
    val counterparty: String?,
    val paid: Boolean,
)
