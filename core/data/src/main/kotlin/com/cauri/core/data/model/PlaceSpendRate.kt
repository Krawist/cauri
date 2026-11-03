package com.cauri.core.data.model

/**
 * How often the visits to a shop lead to an expense there.
 *
 * @property name Name of the shop.
 * @property visits Visits left there.
 * @property withExpense Those followed by an expense confirmed there, up to 15 minutes after the exit.
 */
data class PlaceSpendRate(val name: String, val visits: Int, val withExpense: Int)
