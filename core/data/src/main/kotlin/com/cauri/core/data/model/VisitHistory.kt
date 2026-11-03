package com.cauri.core.data.model

/**
 * The visit behind a card, and how its place usually goes.
 *
 * @property visitId Visit whose card it is.
 * @property placeVisits Visits to the place left before, this one apart.
 * @property visitsWithExpense How many of them were followed by an expense there.
 */
data class VisitHistory(val visitId: Long, val placeVisits: Int, val visitsWithExpense: Int)
