package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.CategoryDao
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.Category
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import java.time.YearMonth
import com.cauri.core.data.service.CategoryStyleService
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryColor
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
    private val styles: CategoryStyleService,
    private val clock: Clock,
) : CategoryRepository {
    override val categories: Flow<List<Category>> = categoryDao.observeActive().map { it.map(::toCategory) }

    override val archived: Flow<List<Category>> = categoryDao.observeArchived().map { it.map(::toCategory) }

    // The month is read as the flow starts: the app lives across the first of a month.
    override val monthTotals: Flow<Map<Long, Long>> = flow {
        val since = YearMonth.now(clock).atDay(1).atStartOfDay(clock.zone).toInstant()
        emitAll(categoryDao.observeTotals(since).map { totals -> totals.associate { it.categoryId to it.amount } })
    }

    override suspend fun createCategories(categories: List<NewCategory>): List<Long> {
        val first = categoryDao.getNextPosition()
        return categoryDao.insertAll(
            categories.mapIndexed { index, category ->
                CategoryEntity(
                    name = category.name.trim(),
                    parentId = category.parentId,
                    nature = category.nature,
                    status = CategoryStatus.ACTIVE,
                    icon = category.icon,
                    color = category.color,
                    position = first + index,
                )
            },
        )
    }

    override suspend fun update(id: Long, name: String, icon: CategoryIcon, color: CategoryColor) {
        categoryDao.get(id)?.let { categoryDao.update(it.copy(name = name.trim(), icon = icon, color = color)) }
    }

    override suspend fun archive(id: Long) = categoryDao.setStatus(id, CategoryStatus.ARCHIVED)

    override suspend fun restore(id: Long) = categoryDao.setStatus(id, CategoryStatus.ACTIVE)

    override suspend fun reorder(ids: List<Long>) = categoryDao.reorder(ids)

    override suspend fun merge(from: Long, into: Long) {
        val target = categoryDao.get(into) ?: return
        if (from == into || categoryDao.get(from) == null) return
        categoryDao.merge(from = from, into = into, intoTop = target.parentId ?: target.id)
    }

    // The user's icon and colour, else Cauri's from the name.
    private fun toCategory(entity: CategoryEntity): Category {
        val icon = entity.icon ?: styles.icon(entity.name, entity.nature)
        return Category(
            id = entity.id,
            name = entity.name,
            nature = entity.nature,
            parentId = entity.parentId,
            icon = icon,
            color = entity.color ?: styles.color(icon),
        )
    }

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
