package com.cauri.core.data.repository

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.PendingOperation
import kotlinx.coroutines.flow.Flow

/** The user's categories of transactions. */
interface CategoryRepository {
    /**
     * The active categories, sub-categories included, in the user's order then by name, again after each change; their
     * icon and colour Cauri's when the user chose none.
     */
    val categories: Flow<List<Category>>

    /** The archived categories, by name, again after each change. */
    val archived: Flow<List<Category>>

    /**
     * What each category counted this month, the expenses and the incomes confirmed or waiting, in minor units of XAF,
     * by category as the transactions have it (a sub-category apart), again after each change.
     */
    val monthTotals: Flow<Map<Long, Long>>

    /** Creates [categories] as active ones at the end of the list, all or none, and returns their ids in order. */
    suspend fun createCategories(categories: List<NewCategory>): List<Long>

    /** Renames the category [id] to [name] and dresses it with [icon] and [color]. */
    suspend fun update(id: Long, name: String, icon: CategoryIcon, color: CategoryColor)

    /** Archives the category [id] and its sub-categories: their history stays, they are no longer offered. */
    suspend fun archive(id: Long)

    /** Brings back the archived category [id] and its sub-categories. */
    suspend fun restore(id: Long)

    /** Puts the categories in the order of [ids]. */
    suspend fun reorder(ids: List<Long>)

    /**
     * Moves everything of the category [from] into [into] (its transactions, sub-categories, habits, budget, places,
     * fixed costs and envelopes), then deletes it. It cannot be undone.
     */
    suspend fun merge(from: Long, into: Long)

    /**
     * The [limit] categories the user most likely wants for an entry of [type] on [accountId] now: the ones used most
     * on that account around this time of day over the last 90 days, then at any hour, then by name.
     */
    suspend fun suggested(accountId: Long, type: EntryType, limit: Int): List<Category>

    /**
     * The [limit] categories the user most likely wants for [operation], an expense or an income to confirm: the ones
     * chosen before with its merchant, then at its place, the recent ones weighing more, then the one it has (a shop's
     * own), then those [suggested] would give for its account. None for a transfer.
     */
    suspend fun predicted(operation: PendingOperation, limit: Int): List<Category>
}
