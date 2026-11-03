package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import java.time.Instant
import kotlinx.coroutines.flow.Flow

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

    /** Remembers that [templateVersion] read the raw signal [rawSignalId]: it is no longer replayed. */
    @Query("UPDATE raw_signals SET templateVersion = :templateVersion WHERE id = :rawSignalId")
    suspend fun markParsed(rawSignalId: Long, templateVersion: String)

    /**
     * Inserts [transactions], read in the raw signal [rawSignalId] by [templateVersion], and marks the signal read: all
     * or none.
     */
    @Transaction
    suspend fun insertParsed(transactions: List<TransactionEntity>, rawSignalId: Long, templateVersion: String) {
        insertAll(transactions)
        markParsed(rawSignalId = rawSignalId, templateVersion = templateVersion)
    }

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    /** The two sides of a transfer. */
    @Query("SELECT * FROM transactions WHERE transferGroup = :transferGroup")
    suspend fun getByTransferGroup(transferGroup: String): List<TransactionEntity>

    /** The transactions of [accountId] that moved money after [after], not [excluded], oldest first. */
    @Query(
        "SELECT * FROM transactions WHERE accountId = :accountId AND occurredAt > :after AND status != :excluded " +
            "ORDER BY occurredAt",
    )
    fun observeOnAccountAfter(
        accountId: Long,
        after: Instant,
        excluded: TransactionStatus,
    ): Flow<List<TransactionEntity>>

    /** Whether [accountId] has a transaction of [type] after [after], not [excluded]. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM transactions WHERE accountId = :accountId AND type = :type " +
            "AND occurredAt > :after AND status != :excluded)",
    )
    suspend fun existsOnAccountAfter(
        accountId: Long,
        type: TransactionType,
        after: Instant,
        excluded: TransactionStatus,
    ): Boolean

    /** The transactions with [status], newest first. */
    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY occurredAt DESC")
    fun observeByStatus(status: TransactionStatus): Flow<List<TransactionEntity>>
}
