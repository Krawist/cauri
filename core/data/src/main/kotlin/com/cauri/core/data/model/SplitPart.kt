package com.cauri.core.data.model

/**
 * A part of an expense or an income split across several categories.
 *
 * @property categoryId Category of the part.
 * @property amount Amount of the part, in minor units of the account's currency.
 */
data class SplitPart(val categoryId: Long, val amount: Long)
