package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.BudgetDao
import com.cauri.core.data.database.entity.BudgetEntity
import com.cauri.core.data.database.entity.BudgetStatus
import com.cauri.core.data.model.Budget
import com.cauri.core.data.model.BudgetSuggestion
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.service.BudgetService
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/** Default implementation for [BudgetRepository]. */
@Singleton
internal class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val categories: CategoryRepository,
    private val service: BudgetService,
    private val clock: Clock,
) : BudgetRepository {
    override val budgets: Flow<List<Budget>> = combine(budgetDao.observeAll(), categories.categories) { all, active ->
        val names = active.associate { it.id to it.name }
        all.filter { it.status == BudgetStatus.ACTIVE }.mapNotNull { budget ->
            names[budget.categoryId]?.let {
                Budget(categoryId = budget.categoryId, categoryName = it, amount = budget.amount)
            }
        }.sortedBy { it.categoryName }
    }

    override suspend fun suggestions(): List<BudgetSuggestion> {
        val now = clock.instant()
        val set = budgetDao.observeAll().first().map { it.categoryId }.toSet()
        // A budget is for a top-level category: its sub-categories count in it.
        val expenses = categories.categories.first()
            .filter { it.nature == CategoryNature.EXPENSE && it.parentId == null && it.id !in set }
        val suggested = service.suggest(
            spending = budgetDao.getSpending(since = now - BudgetService.WINDOW),
            firstTransactionAt = budgetDao.getFirstTransactionAt(),
            now = now,
        )
        return expenses.mapNotNull { category ->
            suggested[category.id]?.let {
                BudgetSuggestion(categoryId = category.id, categoryName = category.name, amount = it)
            }
        }
    }

    override suspend fun setBudget(categoryId: Long, amount: Long, suggested: Long?) = budgetDao.upsert(
        BudgetEntity(
            categoryId = categoryId,
            amount = amount,
            status = BudgetStatus.ACTIVE,
            suggestedAmount = suggested,
            updatedAt = clock.instant(),
        ),
    )

    override suspend fun ignore(categoryId: Long) = budgetDao.upsert(
        BudgetEntity(
            categoryId = categoryId,
            amount = 0,
            status = BudgetStatus.IGNORED,
            suggestedAmount = null,
            updatedAt = clock.instant(),
        ),
    )
}
