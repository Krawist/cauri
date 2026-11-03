package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.database.entity.VisitEntity
import com.cauri.core.data.model.PlaceSpendRate
import java.time.Instant

// One function per read of the decisions and of the context they are taken in.
@Suppress("TooManyFunctions")
@Dao
internal interface DecisionLogDao {
    /** Inserts [decision] and returns its generated id. */
    @Insert
    suspend fun insert(decision: DecisionLogEntity): Long

    /** Records the outcome of a decision. */
    @Update
    suspend fun update(decision: DecisionLogEntity)

    /** The decisions taken since [from], oldest first: the training window of the bandit. */
    @Query("SELECT * FROM decision_log WHERE decidedAt >= :from ORDER BY decidedAt")
    suspend fun getSince(from: Instant): List<DecisionLogEntity>

    /** The decisions with no outcome yet. */
    @Query("SELECT * FROM decision_log WHERE outcome IS NULL ORDER BY decidedAt")
    suspend fun getOpen(): List<DecisionLogEntity>

    /** When a card was confirmed or set aside as « Rien dépensé » since [since], the oldest first. */
    @Query(
        "SELECT outcomeAt FROM decision_log WHERE outcome IN ('CONFIRMED', 'NOTHING_SPENT') " +
            "AND outcomeAt >= :since ORDER BY outcomeAt",
    )
    suspend fun getAnswerTimes(since: Instant): List<Instant>

    /** The decisions closed and not left out, taken since [since], oldest first: what the bandit learns from. */
    @Query(
        "SELECT * FROM decision_log WHERE outcome IS NOT NULL AND excluded IS NULL AND decidedAt >= :since " +
            "ORDER BY id",
    )
    suspend fun getTrainingSet(since: Instant): List<DecisionLogEntity>

    /**
     * How often the visits to each shop left since [since] were followed by an expense confirmed there, up to 15
     * minutes after the exit (in milliseconds, as the instants are stored).
     */
    @Query(
        "SELECT places.name AS name, COUNT(*) AS visits, SUM(CASE WHEN EXISTS (SELECT 1 FROM transactions " +
            "WHERE transactions.placeId = visits.placeId AND transactions.type = 'EXPENSE' " +
            "AND transactions.status = 'CONFIRMED' AND transactions.amount > 0 " +
            "AND transactions.occurredAt >= visits.enteredAt AND transactions.occurredAt <= visits.leftAt + 900000) " +
            "THEN 1 ELSE 0 END) AS withExpense FROM visits JOIN places ON places.id = visits.placeId " +
            "WHERE places.kind = 'SHOP' AND visits.leftAt >= :since GROUP BY places.id ORDER BY places.name",
    )
    suspend fun getPlaceSpendRates(since: Instant): List<PlaceSpendRate>

    /** The visit [id]; `null` when it was deleted with its place. */
    @Query("SELECT * FROM visits WHERE id = :id")
    suspend fun getVisit(id: Long): VisitEntity?

    /** Whether an expense was typed by hand at the place [placeId] from [from] to [to]. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM transactions WHERE placeId = :placeId AND origin = 'MANUAL' " +
            "AND type = 'EXPENSE' AND occurredAt BETWEEN :from AND :to)",
    )
    suspend fun hasExpenseTypedAt(placeId: Long, from: Instant, to: Instant): Boolean

    /** Whether a decision was taken about the transaction [transactionId]: the engine decides once per card. */
    @Query("SELECT EXISTS(SELECT 1 FROM decision_log WHERE transactionId = :transactionId)")
    suspend fun hasDecision(transactionId: Long): Boolean

    /** How many reminders were notified from [since]. */
    @Query("SELECT COUNT(*) FROM decision_log WHERE action = 'NOTIFY_NOW' AND decidedAt >= :since")
    suspend fun countNotifiedSince(since: Instant): Int

    /** When the last reminder was notified; `null` before the first. */
    @Query("SELECT MAX(decidedAt) FROM decision_log WHERE action = 'NOTIFY_NOW'")
    suspend fun getLastNotifiedAt(): Instant?

    /** When the last transaction counted happened, a card still at 0 left out; `null` before the first. */
    @Query("SELECT MAX(occurredAt) FROM transactions WHERE status IN ('DETECTED', 'CONFIRMED') AND amount > 0")
    suspend fun getLastTransactionAt(): Instant?

    /** The visit whose card is the transaction [transactionId]; `null` when the transaction is no visit's card. */
    @Query("SELECT * FROM visits WHERE transactionId = :transactionId")
    suspend fun getVisitOfCard(transactionId: Long): VisitEntity?

    /** How many visits to the place [placeId] were left, the visit [exceptVisit] apart. */
    @Query("SELECT COUNT(*) FROM visits WHERE placeId = :placeId AND leftAt IS NOT NULL AND id != :exceptVisit")
    suspend fun countVisits(placeId: Long, exceptVisit: Long): Int

    /**
     * How many of those visits were followed by an expense there, confirmed, up to 15 minutes after the exit (in
     * milliseconds, as the instants are stored).
     */
    @Query(
        "SELECT COUNT(*) FROM visits WHERE placeId = :placeId AND leftAt IS NOT NULL AND id != :exceptVisit " +
            "AND EXISTS (SELECT 1 FROM transactions WHERE transactions.placeId = visits.placeId " +
            "AND transactions.type = 'EXPENSE' AND transactions.status = 'CONFIRMED' AND transactions.amount > 0 " +
            "AND transactions.occurredAt >= visits.enteredAt AND transactions.occurredAt <= visits.leftAt + 900000)",
    )
    suspend fun countVisitsWithExpense(placeId: Long, exceptVisit: Long): Int
}
