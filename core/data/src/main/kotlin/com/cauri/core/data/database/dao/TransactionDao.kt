package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.service.ProposalOutcome
import java.time.Instant
import kotlinx.coroutines.flow.Flow

// One query per read or write the repositories need on the transactions.
@Suppress("TooManyFunctions")
@Dao
internal interface TransactionDao {
    /**
     * Inserts [transaction] and returns its generated id. Fails with a constraint error when its raw signal already
     * created a transaction.
     */
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    /** Inserts [transactions] all or none: none when one of their raw signals already created a transaction. */
    @Insert
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    /** The two sides of a transfer. */
    @Query("SELECT * FROM transactions WHERE transferGroup = :transferGroup")
    suspend fun getByTransferGroup(transferGroup: String): List<TransactionEntity>

    /** The transactions of [accountId] with [status] that happened between [from] and [to]. */
    @Query(
        "SELECT * FROM transactions WHERE accountId = :accountId AND status = :status " +
            "AND occurredAt BETWEEN :from AND :to",
    )
    suspend fun getAround(
        accountId: Long,
        status: TransactionStatus,
        from: Instant,
        to: Instant,
    ): List<TransactionEntity>

    /** The transactions of [accountId] that moved money after [after], none with an [excluded] status, oldest first. */
    @Query(
        "SELECT * FROM transactions WHERE accountId = :accountId AND occurredAt > :after " +
            "AND status NOT IN (:excluded) ORDER BY occurredAt",
    )
    fun observeOnAccountAfter(
        accountId: Long,
        after: Instant,
        excluded: List<TransactionStatus>,
    ): Flow<List<TransactionEntity>>

    /** Whether [accountId] has a transaction of [type] after [after], none with an [excluded] status. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM transactions WHERE accountId = :accountId AND type = :type " +
            "AND occurredAt > :after AND status NOT IN (:excluded))",
    )
    suspend fun existsOnAccountAfter(
        accountId: Long,
        type: TransactionType,
        after: Instant,
        excluded: List<TransactionStatus>,
    ): Boolean

    /** The transactions with [status], newest first. */
    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY occurredAt DESC")
    fun observeByStatus(status: TransactionStatus): Flow<List<TransactionEntity>>

    /**
     * The confirmed transactions that came with a proposal of Cauri, next to what the user kept; a split operation
     * counts its parts in its amount.
     */
    @Query(
        "SELECT t.placeId, t.counterparty, r.templateVersion, t.proposedCategoryId, t.categoryId, " +
            "t.proposedAmount, t.amount + COALESCE((SELECT SUM(s.amount) FROM transactions AS s " +
            "WHERE s.splitGroup = t.splitGroup AND s.id != t.id), 0) AS amount " +
            "FROM transactions AS t LEFT JOIN raw_signals AS r ON r.id = t.rawSignalId " +
            "WHERE t.status = 'CONFIRMED' AND (t.proposedCategoryId IS NOT NULL OR t.proposedAmount IS NOT NULL)",
    )
    suspend fun getProposalOutcomes(): List<ProposalOutcome>
}
