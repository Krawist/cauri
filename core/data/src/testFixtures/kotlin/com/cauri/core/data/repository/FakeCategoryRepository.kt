package com.cauri.core.data.repository

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [CategoryRepository] for tests: created categories get the ids 1, 2, 3… in order; the test sets the
 * categories, the archived ones and the totals; the changes are kept.
 */
class FakeCategoryRepository : CategoryRepository {
    /**
     * A change of [update].
     *
     * @property id The category.
     * @property name Its new name.
     * @property icon Its new icon.
     * @property color Its new colour.
     */
    data class Updated(val id: Long, val name: String, val icon: CategoryIcon, val color: CategoryColor)

    val created = mutableListOf<NewCategory>()

    override val categories = MutableStateFlow<List<Category>>(emptyList())

    override val archived = MutableStateFlow<List<Category>>(emptyList())

    override val monthTotals = MutableStateFlow<Map<Long, Long>>(emptyMap())

    val updated = mutableListOf<Updated>()

    val archivedIds = mutableListOf<Long>()

    val restored = mutableListOf<Long>()

    val orders = mutableListOf<List<Long>>()

    val merges = mutableListOf<Pair<Long, Long>>()

    override suspend fun update(id: Long, name: String, icon: CategoryIcon, color: CategoryColor) {
        updated += Updated(id = id, name = name, icon = icon, color = color)
    }

    override suspend fun archive(id: Long) {
        archivedIds += id
    }

    override suspend fun restore(id: Long) {
        restored += id
    }

    override suspend fun reorder(ids: List<Long>) {
        orders += ids
    }

    override suspend fun merge(from: Long, into: Long) {
        merges += from to into
    }

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
