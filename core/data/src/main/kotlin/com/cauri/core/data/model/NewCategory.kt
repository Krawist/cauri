package com.cauri.core.data.model

/**
 * A top-level category to create, such as one kept from the onboarding pack or added by the user.
 *
 * @property name Name shown to the user (« Transport »).
 * @property nature Whether it counts spending, income or savings.
 */
data class NewCategory(val name: String, val nature: CategoryNature)
