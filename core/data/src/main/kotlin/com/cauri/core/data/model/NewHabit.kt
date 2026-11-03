package com.cauri.core.data.model

/**
 * A habit to create.
 *
 * @property name Name shown on the tile (« Moto »).
 * @property amount Amount of each expense, in minor units of the Cash account's currency.
 * @property categoryId Category of the expenses it records; `null` when none.
 */
data class NewHabit(val name: String, val amount: Long, val categoryId: Long?)
