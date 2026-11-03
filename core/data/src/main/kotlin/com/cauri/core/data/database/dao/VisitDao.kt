package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.VisitEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

// One function per read and write of the visits and of the transactions made during them.
@Suppress("TooManyFunctions")
@Dao
internal interface VisitDao {
    /** Inserts [visit] and returns its generated id. */
    @Insert
    suspend fun insert(visit: VisitEntity): Long

    @Update
    suspend fun update(visit: VisitEntity)

    /** The visits the user has not left yet. */
    @Query("SELECT * FROM visits WHERE leftAt IS NULL")
    suspend fun getOngoing(): List<VisitEntity>

    /** Whether the user is still in the place [placeId], a visit there not left yet. */
    @Query("SELECT EXISTS(SELECT 1 FROM visits WHERE placeId = :placeId AND leftAt IS NULL)")
    suspend fun isOngoing(placeId: Long): Boolean

    /**
     * Opens a visit to the place [placeId] from [enteredAt], unless one is open there already: Android may tell the
     * same stay twice, such as after a restart.
     */
    @Transaction
    suspend fun enter(placeId: Long, enteredAt: Instant) {
        if (!isOngoing(placeId)) insert(VisitEntity(placeId = placeId, enteredAt = enteredAt, leftAt = null))
    }

    /** Closes at [leftAt] the visit to the place [placeId] not left yet; nothing when there is none. */
    @Query("UPDATE visits SET leftAt = :leftAt WHERE placeId = :placeId AND leftAt IS NULL")
    suspend fun leave(placeId: Long, leftAt: Instant)

    @Query("SELECT * FROM visits WHERE id = :id")
    suspend fun getById(id: Long): VisitEntity?

    /**
     * The visits to shops that bring a reminder, left from [since], with no card yet: those to ask about when nothing
     * was paid during them.
     */
    @Query(
        "SELECT visits.*, places.name AS placeName, places.defaultCategoryId AS categoryId, NULL AS proposedAmount " +
            "FROM visits JOIN places ON places.id = visits.placeId WHERE visits.leftAt >= :since " +
            "AND visits.transactionId IS NULL AND places.kind = 'SHOP' AND places.remindAfterVisit = 1",
    )
    suspend fun getWithoutCard(since: Instant): List<VisitAtPlace>

    /** The visits whose card still waits for the user, the latest first, again after each change. */
    @Query(
        "SELECT visits.*, places.name AS placeName, transactions.categoryId AS categoryId, " +
            "transactions.proposedAmount AS proposedAmount FROM visits " +
            "JOIN places ON places.id = visits.placeId JOIN transactions ON transactions.id = visits.transactionId " +
            "WHERE transactions.status = 'DETECTED' ORDER BY visits.leftAt DESC",
    )
    fun observeWaiting(): Flow<List<VisitAtPlace>>

    /** The visits that created a card, again after each change: how long the stay of each card lasted. */
    @Query("SELECT * FROM visits WHERE transactionId IS NOT NULL")
    fun observeWithCard(): Flow<List<VisitEntity>>

    /** The visits not left yet, or left from [since]: those a transaction may still belong to. */
    @Query("SELECT * FROM visits WHERE leftAt IS NULL OR leftAt >= :since")
    suspend fun getRecent(since: Instant): List<VisitEntity>

    /** The confirmed expenses made at the place [placeId], the oldest first: its usual amounts. */
    @Query(
        "SELECT * FROM transactions WHERE placeId = :placeId AND type = 'EXPENSE' AND status = 'CONFIRMED' " +
            "ORDER BY occurredAt",
    )
    suspend fun getExpensesAt(placeId: Long): List<TransactionEntity>

    /**
     * The expenses counted, read in a message or typed, made from [from] to [to]: those a visit may take. A fee goes
     * with its operation, the card of a visit is no payment.
     */
    @Query(
        "SELECT * FROM transactions WHERE type = 'EXPENSE' AND status IN ('DETECTED', 'CONFIRMED') " +
            "AND origin != 'VISIT' AND NOT (origin = 'SIGNAL' AND rawSignalId IS NULL) " +
            "AND occurredAt >= :from AND occurredAt <= :to",
    )
    suspend fun getPaymentsBetween(from: Instant, to: Instant): List<TransactionEntity>

    @Query("UPDATE transactions SET placeId = :placeId WHERE id IN (:ids) AND placeId IS NULL")
    suspend fun setPlace(ids: List<Long>, placeId: Long)

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("UPDATE visits SET transactionId = :transactionId WHERE id = :id")
    suspend fun setTransaction(id: Long, transactionId: Long?)

    /** Creates [card], the expense to confirm of the visit [visitId], all or none. */
    @Transaction
    suspend fun createCard(visitId: Long, card: TransactionEntity) {
        setTransaction(id = visitId, transactionId = insertTransaction(card))
    }

    /** Takes back the card of [visit] while it still waits: a payment was made during it. All or none. */
    @Transaction
    suspend fun removeWaitingCard(visit: VisitEntity) {
        val card = visit.transactionId ?: return
        if (deleteWaiting(card) > 0) setTransaction(id = visit.id, transactionId = null)
    }

    /** Deletes the transaction [id] while it waits for the user, and returns how many it deleted. */
    @Query("DELETE FROM transactions WHERE id = :id AND status = 'DETECTED'")
    suspend fun deleteWaiting(id: Long): Int

    /** Confirms the card [id] of a visit with [amount], in minor units: split in a cash count. */
    @Query("UPDATE transactions SET amount = :amount, status = 'CONFIRMED' WHERE id = :id AND status = 'DETECTED'")
    suspend fun confirmCard(id: Long, amount: Long)

    /** Sets aside the cards of the visits still waiting from before [before]: the cash count holds them. */
    @Query(
        "UPDATE transactions SET status = 'DISMISSED' WHERE origin = 'VISIT' AND status = 'DETECTED' " +
            "AND occurredAt < :before",
    )
    suspend fun dismissCardsBefore(before: Instant)

    /** Confirms the cards of [amounts], by transaction, and sets aside the others from before [at], all or none. */
    @Transaction
    suspend fun settle(amounts: Map<Long, Long>, at: Instant) {
        amounts.forEach { (id, amount) -> confirmCard(id = id, amount = amount) }
        dismissCardsBefore(at)
    }
}
