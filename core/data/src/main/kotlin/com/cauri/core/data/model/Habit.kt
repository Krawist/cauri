package com.cauri.core.data.model

/**
 * A habitual cash expense, recorded in one tap from the home screen (« Moto 500 F »).
 *
 * @property id Identifier of the habit.
 * @property name Name shown on the tile and given to the expenses it records.
 * @property amount Amount of each expense, in minor units of the Cash account's currency.
 * @property categoryId Category of the expenses it records; `null` when none.
 */
data class Habit(val id: Long, val name: String, val amount: Long, val categoryId: Long?)
