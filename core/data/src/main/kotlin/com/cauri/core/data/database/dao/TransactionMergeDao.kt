package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionMergeEntity
import com.cauri.core.data.database.entity.TransactionStatus
import java.time.Instant

/** The writes that record a parsed signal or merge two sides of the same operation, each all or none. */
@Dao
internal interface TransactionMergeDao {
    /**
     * Inserts [transactions], read in the raw signal [rawSignalId] by [templateVersion], and marks the signal read.
     * With [merge], the first transaction is kept for the manual entry [entry], set aside, and the merge is logged.
     */
    @Transaction
    suspend fun insertParsed(
        transactions: List<TransactionEntity>,
        rawSignalId: Long,
        templateVersion: String,
        entry: TransactionEntity?,
        merge: TransactionMergeEntity?,
    ) {
        val ids = insertTransactions(transactions)
        markParsed(rawSignalId = rawSignalId, templateVersion = templateVersion)
        if (entry != null && merge != null) {
            updateTransaction(entry.copy(status = TransactionStatus.MERGED))
            insertMerge(merge.copy(keptId = ids.first()))
        }
    }

    /**
     * Inserts the manual entry [entry], set aside, updates [kept], the detected transaction of the same operation, with
     * the user's values, logs [merge] and returns its id.
     */
    @Transaction
    suspend fun insertMerged(entry: TransactionEntity, kept: TransactionEntity, merge: TransactionMergeEntity): Long {
        val entryId = insertTransactions(listOf(entry.copy(status = TransactionStatus.MERGED))).first()
        updateTransaction(kept)
        return insertMerge(merge.copy(mergedId = entryId))
    }

    /** Undoes [merge]: the kept transaction gets its values back and the manual entry counts again. */
    @Transaction
    suspend fun undoMerge(merge: TransactionMergeEntity, undoneAt: Instant) {
        getTransaction(merge.keptId)?.let {
            updateTransaction(
                it.copy(
                    status = merge.keptStatus,
                    categoryId = merge.keptCategoryId,
                    note = merge.keptNote,
                    placeId = merge.keptPlaceId,
                ),
            )
        }
        getTransaction(merge.mergedId)?.let { updateTransaction(it.copy(status = TransactionStatus.CONFIRMED)) }
        updateMerge(merge.copy(undoneAt = undoneAt))
    }

    @Query("SELECT * FROM transaction_merges WHERE id = :id")
    suspend fun getMerge(id: Long): TransactionMergeEntity?

    /** Inserts [transactions] and returns their generated ids in order. */
    @Insert
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): TransactionEntity?

    /** Remembers that [templateVersion] read the raw signal [rawSignalId]: it is no longer replayed. */
    @Query("UPDATE raw_signals SET templateVersion = :templateVersion WHERE id = :rawSignalId")
    suspend fun markParsed(rawSignalId: Long, templateVersion: String)

    /** Logs [merge] and returns its generated id. */
    @Insert
    suspend fun insertMerge(merge: TransactionMergeEntity): Long

    @Update
    suspend fun updateMerge(merge: TransactionMergeEntity)
}
