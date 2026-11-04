package com.cauri.core.data.model

/**
 * An expense paid every month that Cauri asks about: is it a fixed cost?
 *
 * @property cost The fixed cost it would be: the merchant (or the category) and the usual amount and day.
 * @property months Months it was paid in, one payment each.
 */
data class FixedCostSuggestion(val cost: NewFixedCost, val months: Int)
