package com.cauri.feature.onboarding

/**
 * A habit to create at the end of the onboarding, linked to its category by name since the category does not exist
 * yet.
 *
 * @property name Name of the habit, in the user's language.
 * @property amount Amount of each expense, in FCFA.
 * @property categoryName Name of the category it records into, one of the categories chosen in the same step.
 */
internal data class FirstHabit(val name: String, val amount: Long, val categoryName: String)
