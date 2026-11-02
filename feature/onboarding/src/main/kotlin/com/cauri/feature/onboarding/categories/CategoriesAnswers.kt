package com.cauri.feature.onboarding.categories

/**
 * The categories chosen during the onboarding.
 *
 * @property pack Categories kept from the pack, in the pack's order.
 * @property added Names of the categories the user added.
 */
internal data class CategoriesAnswers(val pack: List<PackCategory>, val added: List<String>)
