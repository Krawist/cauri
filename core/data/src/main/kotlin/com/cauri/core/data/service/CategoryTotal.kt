package com.cauri.core.data.service

/**
 * What a category counted.
 *
 * @property categoryId The category, as the transactions have it: a sub-category is counted apart from its parent.
 * @property amount In minor units of XAF.
 */
internal data class CategoryTotal(val categoryId: Long, val amount: Long)
