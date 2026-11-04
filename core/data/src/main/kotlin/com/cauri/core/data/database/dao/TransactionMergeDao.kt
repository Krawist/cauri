package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.AccountBalanceEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionMergeEntity
import com.cauri.core.data.database.entity.TransactionStatus
import java.time.Instant

/** The writes that record a parsed signal or merge two sides of the same operation, each all or none. */
// One function per statement of these writes: the count grows with what a parsed signal records.
@Suppress("TooManyFunctions")
@Dao
internal interface TransactionMergeDao {
    /**
     * Inserts [transactions] and the account's [balance], read in the raw signal [rawSignalId] by [templateVersion],
     * and marks the signal read. With [merge], the first transaction is kept for the manual entry [entry], set aside,
     * and the merge is logged.
     */
    /**
     * The balance [accountId] had before [at], strictly: never one a message of the same operation gave; `null`
     * before the first.
     */
    @Query(
        "SELECT amount FROM account_balances WHERE accountId = :accountId AND at < :at " +
            "ORDER BY at DESC, id DESC LIMIT 1",
    )
    suspend fun getBalanceBefore(accountId: Long, at: Instant): Long?

    @Transaction
    @Suppress("LongParameterList") // Everything one signal writes, in one transaction.
    suspend fun insertParsed(
        transactions: List<TransactionEntity>,
        balance: AccountBalanceEntity?,
        rawSignalId: Long,
        templateVersion: String,
        entry: TransactionEntity?,
        merge: TransactionMergeEntity?,
    ) {
        val ids = insertTransactions(transactions)
        balance?.let { insertBalance(it) }
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
    /**
     * Attaches the message [rawSignalId] read by [templateVersion] to a transfer between the user's accounts recorded
     * from the message of its other side, all or none: both sides as [debit] and [credit], the [fees] it brings and the
     * [balance] it gave.
     */
    @Transaction
    @Suppress("LongParameterList") // Everything one signal writes, in one transaction.
    suspend fun attachSide(
        debit: TransactionEntity,
        credit: TransactionEntity,
        fees: List<TransactionEntity>,
        balance: AccountBalanceEntity?,
        rawSignalId: Long,
        templateVersion: String,
    ) {
        updateTransaction(debit)
        updateTransaction(credit)
        insertTransactions(fees)
        balance?.let { insertBalance(it) }
        markParsed(rawSignalId = rawSignalId, templateVersion = templateVersion)
    }

    @Transaction
    suspend fun insertMerged(entry: TransactionEntity, kept: TransactionEntity, merge: TransactionMergeEntity): Long {
        val entryId = insertTransactions(listOf(entry.copy(status = TransactionStatus.MERGED))).first()
        updateTransaction(kept)
        return insertMerge(merge.copy(mergedId = entryId))
    }

    /**
     * Undoes [merge]: the kept transaction gets its values back, the amount read in its message and no parts when the
     * user split it since, and the manual entry counts again.
     */
    @Transaction
    suspend fun undoMerge(merge: TransactionMergeEntity, undoneAt: Instant) {
        getTransaction(merge.keptId)?.let {
            it.splitGroup?.let { group -> deleteParts(splitGroup = group, keptId = it.id) }
            updateTransaction(
                it.copy(
                    status = merge.keptStatus,
                    amount = it.proposedAmount ?: it.amount,
                    categoryId = merge.keptCategoryId,
                    note = merge.keptNote,
                    placeId = merge.keptPlaceId,
                    splitGroup = null,
                ),
            )
        }
        getTransaction(merge.mergedId)?.let { updateTransaction(it.copy(status = TransactionStatus.CONFIRMED)) }
        updateMerge(merge.copy(undoneAt = undoneAt))
    }

    @Query("SELECT * FROM transaction_merges WHERE id = :id")
    suspend fun getMerge(id: Long): TransactionMergeEntity?

    /** The merge that holds on the kept transaction [keptId]; `null` when none does. */
    @Query("SELECT id FROM transaction_merges WHERE keptId = :keptId AND undoneAt IS NULL")
    suspend fun getHeldMergeId(keptId: Long): Long?

    /** Deletes the parts of the split [splitGroup], all but the transaction [keptId] that keeps the rest. */
    @Query("DELETE FROM transactions WHERE splitGroup = :splitGroup AND id != :keptId")
    suspend fun deleteParts(splitGroup: String, keptId: Long)

    /** Inserts [transactions] and returns their generated ids in order. */
    @Insert
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Insert
    suspend fun insertBalance(balance: AccountBalanceEntity)

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
