package com.cauri.core.data.repository

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.PendingOperation
import kotlinx.coroutines.flow.Flow

/** The user's categories of transactions. */
interface CategoryRepository {
    /** The active categories, by name, again after each change. */
    val categories: Flow<List<Category>>

    /**
     * Creates [categories] as active top-level categories, all or none, and returns their ids in the same order.
     */
    suspend fun createCategories(categories: List<NewCategory>): List<Long>

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
