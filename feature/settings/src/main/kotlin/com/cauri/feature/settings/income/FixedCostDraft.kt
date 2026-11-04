package com.cauri.feature.settings.income

/**
 * A fixed cost being added or edited in its sheet.
 *
 * @property id Fixed cost being edited; `null` for a new one.
 * @property name Name as typed.
 * @property amount Amount a month as typed, digits only, in FCFA; empty when not given yet.
 * @property dayOfMonth Day of the month it is paid, from 1 to 31.
 * @property categoryId Category of its payments, kept from the fixed cost edited; `null` when none.
 * @property counterparty Merchant its payments name, kept from the fixed cost edited; `null` when none.
 * @property nameMissing Whether the user tried to save without a name.
 * @property amountMissing Whether the user tried to save without an amount above 0.
 */
internal data class FixedCostDraft(
    val id: Long? = null,
    val name: String = "",
    val amount: String = "",
    val dayOfMonth: Int = 1,
    val categoryId: Long? = null,
    val counterparty: String? = null,
    val nameMissing: Boolean = false,
    val amountMissing: Boolean = false,
)
