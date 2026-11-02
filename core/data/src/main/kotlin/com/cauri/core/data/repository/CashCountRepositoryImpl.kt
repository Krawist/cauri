package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.CashCountDao
import com.cauri.core.data.database.entity.CashCountEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [CashCountRepository]. */
@Singleton
internal class CashCountRepositoryImpl @Inject constructor(private val cashCountDao: CashCountDao) :
    CashCountRepository {
    override suspend fun addCount(amount: Long, countedAt: Instant, expectedAmount: Long?) {
        cashCountDao.insert(CashCountEntity(countedAt = countedAt, amount = amount, expectedAmount = expectedAmount))
    }
}
