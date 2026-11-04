package com.cauri.core.data.model

/**
 * A category of transactions, on two levels at most.
 *
 * @property id Identifier of the category.
 * @property name Name shown to the user (« Transport »).
 * @property nature Whether it counts spending, income or savings.
 * @property parentId Category this one is a sub-category of (« Taxi » in « Transport »); `null` for a top-level one.
 * @property icon Its icon.
 * @property color The colour of its icon.
 */
data class Category(
    val id: Long,
    val name: String,
    val nature: CategoryNature,
    val parentId: Long? = null,
    val icon: CategoryIcon = CategoryIcon.TAG,
    val color: CategoryColor = CategoryColor.NEUTRAL,
)
