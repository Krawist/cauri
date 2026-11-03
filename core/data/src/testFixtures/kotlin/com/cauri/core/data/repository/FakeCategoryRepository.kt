package com.cauri.core.data.repository

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [CategoryRepository] for tests: created categories get the ids 1, 2, 3… in order. */
class FakeCategoryRepository : CategoryRepository {
    val created = mutableListOf<NewCategory>()

    override val categories = MutableStateFlow<List<Category>>(emptyList())

    override suspend fun createCategories(categories: List<NewCategory>): List<Long> {
        val first = created.size + 1L
        created += categories
        return categories.indices.map { first + it }
    }

    /** What [suggested] answers, by type of entry, whatever the account. */
    val suggestions = mutableMapOf<EntryType, List<Category>>()

    override suspend fun suggested(accountId: Long, type: EntryType, limit: Int): List<Category> =
        suggestions[type].orEmpty().take(limit)

    /** What the merchants led to before, by name: [predicted] gives them first. */
    val predictions = mutableMapOf<String, List<Category>>()

    override suspend fun predicted(operation: PendingOperation, limit: Int): List<Category> {
        val type = when (operation.type) {
            PendingType.EXPENSE -> EntryType.EXPENSE
            PendingType.INCOME -> EntryType.INCOME
            PendingType.TRANSFER -> return emptyList()
        }
        val own = categories.value.filter { it.id == operation.categoryId }
        val learnt = operation.counterparty?.let { predictions[it] }.orEmpty() + own
        return (learnt + suggested(operation.accountId, type, limit)).distinctBy { it.id }.take(limit)
    }
}
