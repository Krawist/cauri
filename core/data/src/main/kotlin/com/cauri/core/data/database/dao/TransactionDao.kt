package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionStatus
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

    @Update
    suspend fun update(transaction: TransactionEntity)

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

    /** The transactions with [status], newest first. */
    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY occurredAt DESC")
    fun observeByStatus(status: TransactionStatus): Flow<List<TransactionEntity>>
}
