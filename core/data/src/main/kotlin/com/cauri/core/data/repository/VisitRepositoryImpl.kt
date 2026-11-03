package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.CashCountDao
import com.cauri.core.data.database.dao.VisitDao
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.entity.VisitEntity
import com.cauri.core.data.model.PendingVisit
import com.cauri.core.data.service.VisitService
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Default implementation for [VisitRepository]. */
@Singleton
internal class VisitRepositoryImpl @Inject constructor(
    private val visitDao: VisitDao,
    cashCountDao: CashCountDao,
    private val visits: VisitService,
) : VisitRepository {
    override val waiting: Flow<List<PendingVisit>> =
        combine(visitDao.observeWaiting(), cashCountDao.observeLatest()) { waiting, count ->
            waiting.mapNotNull {
                val card = it.visit.transactionId ?: return@mapNotNull null
                val left = it.visit.leftAt ?: return@mapNotNull null
                PendingVisit(
                    transactionId = card,
                    placeName = it.placeName,
                    categoryId = it.categoryId,
                    enteredAt = it.visit.enteredAt,
                    leftAt = left,
                    proposedAmount = it.proposedAmount,
                ).takeIf { count == null || left.isAfter(count.countedAt) }
            }
        }

    override suspend fun atHomeOrWork(): Boolean = visitDao.isAtHomeOrWork()

    override suspend fun enter(placeId: Long, at: Instant) = visitDao.enter(placeId = placeId, enteredAt = at)

    override suspend fun leave(placeId: Long, at: Instant) {
        visitDao.leave(placeId = placeId, leftAt = at)
        attachTransactions(at)
        // The shops left with nothing paid: what was spent there, in cash, is asked with a card.
        visitDao.getWithoutCard(since = at - RECENT).forEach { (visit, placeName, categoryId) ->
            val left = visit.leftAt ?: return@forEach
            if (paidDuring(visit).isNotEmpty()) return@forEach
            visitDao.createCard(
                visitId = visit.id,
                card = TransactionEntity(
                    accountId = CASH_ACCOUNT_ID,
                    type = TransactionType.EXPENSE,
                    status = TransactionStatus.DETECTED,
                    origin = TransactionOrigin.VISIT,
                    // Nothing counts before the user says what was spent.
                    amount = 0,
                    occurredAt = left,
                    categoryId = categoryId,
                    placeId = visit.placeId,
                    rawSignalId = null,
                    transferGroup = null,
                    counterparty = placeName,
                    note = null,
                    foreignAmount = null,
                    foreignCurrency = null,
                    proposedCategoryId = categoryId,
                    proposedAmount = visits.usualAmount(visitDao.getExpensesAt(visit.placeId)),
                ),
            )
        }
    }

    override suspend fun attachTransactions(at: Instant) {
        visitDao.getRecent(since = at - RECENT).forEach { visit ->
            val paid = paidDuring(visit, until = at)
            if (paid.isNotEmpty()) {
                visitDao.setPlace(ids = paid.map { it.id }, placeId = visit.placeId)
                visitDao.removeWaitingCard(visit)
            }
        }
    }

    override suspend fun settle(amounts: Map<Long, Long>, at: Instant) = visitDao.settle(amounts = amounts, at = at)

    // The payments made during [visit], read or typed, up to 15 minutes after its exit or else [until].
    private suspend fun paidDuring(visit: VisitEntity, until: Instant? = null) = visitDao.getPaymentsBetween(
        from = visit.enteredAt,
        to = visit.leftAt?.plus(VisitService.AFTER_EXIT) ?: until ?: Instant.now(),
    ).filter { visits.covers(visit, it.occurredAt) && (it.placeId == null || it.placeId == visit.placeId) }

    private companion object {
        // A transaction typed or read later than a day after a visit is not of it.
        val RECENT: Duration = Duration.ofDays(1)
    }
}
