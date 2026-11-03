package com.cauri.feature.onboarding.categories

/**
 * The categories chosen during the onboarding.
 *
 * @property pack Categories kept from the pack, in the pack's order.
 * @property added Names of the categories the user added.
 * @property habits First one-tap cash expenses, those of the kept pack categories, in the order of the home screen.
 */
internal data class CategoriesAnswers(
    val pack: List<PackCategory>,
    val added: List<String>,
    val habits: List<PackHabit>,
)
