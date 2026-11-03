package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.AccountDao
import com.cauri.core.data.database.dao.CashCountDao
import com.cauri.core.data.database.dao.CategoryDao
import com.cauri.core.data.database.dao.TransactionDao
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.MonthSpending
import com.cauri.core.data.service.ActivityService
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Default implementation for [ActivityRepository]. */
@Singleton
internal class ActivityRepositoryImpl @Inject constructor(
    transactionDao: TransactionDao,
    cashCountDao: CashCountDao,
    accountDao: AccountDao,
    categoryDao: CategoryDao,
    private val activity: ActivityService,
    private val clock: Clock,
) : ActivityRepository {
    override val lines: Flow<List<ActivityLine>> = combine(
        transactionDao.observeHistory(),
        cashCountDao.observeAll(),
        accountDao.observeAll(),
        // Every category, hidden ones too: an old expense keeps its name.
        categoryDao.observeAll(),
    ) { transactions, counts, accounts, categories ->
        activity.lines(
            transactions = transactions,
            counts = counts,
            accounts = accounts.associateBy { it.id },
            categories = categories.associate { it.id to it.name },
        )
    }

    override val monthSpending: Flow<MonthSpending> = lines.map { lines ->
        val month = LocalDate.now(clock).withDayOfMonth(1).atStartOfDay(clock.zone).toInstant()
        activity.spending(lines = lines, from = month)
    }
}
