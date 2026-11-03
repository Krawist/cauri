package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.CategoryDao
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.service.CategoryPredictionService
import com.cauri.core.data.service.CategorySuggestionService
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Default implementation for [CategoryRepository]. */
@Singleton
internal class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val suggestions: CategorySuggestionService,
    private val predictions: CategoryPredictionService,
    private val clock: Clock,
) : CategoryRepository {
    override val categories: Flow<List<Category>> = categoryDao.observeActive().map { categories ->
        categories.map { Category(id = it.id, name = it.name, nature = it.nature) }
    }

    override suspend fun createCategories(categories: List<NewCategory>): List<Long> = categoryDao.insertAll(
        categories.map {
            CategoryEntity(name = it.name, parentId = null, nature = it.nature, status = CategoryStatus.ACTIVE)
        },
    )

    override suspend fun suggested(accountId: Long, type: EntryType, limit: Int): List<Category> {
        val now = clock.instant()
        val (transactionType, nature) = when (type) {
            EntryType.EXPENSE -> TransactionType.EXPENSE to CategoryNature.EXPENSE
            EntryType.INCOME -> TransactionType.INCOME to CategoryNature.INCOME
        }
        return suggestions.suggest(
            uses = categoryDao.getUses(
                accountId = accountId,
                type = transactionType,
                since = now - CategorySuggestionService.HISTORY,
            ),
            categories = categories.first().filter { it.nature == nature },
            at = now,
            zone = clock.zone,
            limit = limit,
        )
    }

    override suspend fun predicted(operation: PendingOperation, limit: Int): List<Category> {
        val type = when (operation.type) {
            PendingType.EXPENSE -> EntryType.EXPENSE
            PendingType.INCOME -> EntryType.INCOME
            PendingType.TRANSFER -> return emptyList()
        }
        val now = clock.instant()
        val since = now - CategoryPredictionService.HISTORY
        val transactionType = type.toTransactionType()
        val merchant = operation.counterparty?.takeIf { it.isNotBlank() }
            ?.let { predictions.predict(categoryDao.getUsesWith(it, transactionType, since), now) }.orEmpty()
        val place = operation.placeId
            ?.let { predictions.predict(categoryDao.getUsesAt(it, transactionType, since), now) }.orEmpty()
        val byId = categories.first().filter { it.nature == type.toNature() }.associateBy { it.id }
        val learnt = (merchant + place + listOfNotNull(operation.categoryId)).mapNotNull { byId[it] }
        return (learnt + suggested(accountId = operation.accountId, type = type, limit = limit))
            .distinctBy { it.id }
            .take(limit)
    }
}

private fun EntryType.toTransactionType() = when (this) {
    EntryType.EXPENSE -> TransactionType.EXPENSE
    EntryType.INCOME -> TransactionType.INCOME
}

private fun EntryType.toNature() = when (this) {
    EntryType.EXPENSE -> CategoryNature.EXPENSE
    EntryType.INCOME -> CategoryNature.INCOME
}
