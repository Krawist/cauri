package com.cauri.core.data.model

/**
 * A category of transactions.
 *
 * @property id Identifier of the category.
 * @property name Name shown to the user (« Transport »).
 * @property nature Whether it counts spending, income or savings.
 */
data class Category(val id: Long, val name: String, val nature: CategoryNature)
