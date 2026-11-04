package com.cauri.core.data.model

/**
 * A category to create, such as one kept from the onboarding pack or added by the user.
 *
 * @property name Name shown to the user (« Transport »).
 * @property nature Whether it counts spending, income or savings; a sub-category's is its parent's.
 * @property parentId Category it is a sub-category of; `null` for a top-level one.
 * @property icon Its icon; `null` to let Cauri choose one from its name.
 * @property color The colour of its icon; `null` to let Cauri choose one from its name.
 */
data class NewCategory(
    val name: String,
    val nature: CategoryNature,
    val parentId: Long? = null,
    val icon: CategoryIcon? = null,
    val color: CategoryColor? = null,
)
