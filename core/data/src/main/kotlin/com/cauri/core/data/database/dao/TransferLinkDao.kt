package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.entity.TransferLinkEntity
import java.time.Instant

/** The transfers between the user's accounts that Cauri links, each write all or none. */
@Dao
internal interface TransferLinkDao {
    /**
     * The expenses and incomes read in signals and not confirmed yet that are no side of a transfer and were never
     * linked, in the account's currency: those that may be two sides of one transfer.
     */
    @Query(
        "SELECT * FROM transactions WHERE origin = 'SIGNAL' AND status = 'DETECTED' AND rawSignalId IS NOT NULL " +
            "AND transferGroup IS NULL AND foreignCurrency IS NULL AND type IN ('EXPENSE', 'INCOME') " +
            "AND id NOT IN (SELECT debitId FROM transfer_links) AND id NOT IN (SELECT creditId FROM transfer_links)",
    )
    suspend fun getLinkable(): List<TransactionEntity>

    /** Turns [debit] and [credit] into the two sides of a transfer, inserts [fee] when there is one, logs [link]. */
    @Transaction
    suspend fun link(
        debit: TransactionEntity,
        credit: TransactionEntity,
        fee: TransactionEntity?,
        link: TransferLinkEntity,
    ) {
        updateTransaction(debit)
        updateTransaction(credit)
        val feeId = fee?.let { insertTransaction(it) }
        insertLink(link.copy(feeId = feeId))
    }

    /** Undoes [link]: the debit and the credit count apart again, with their amounts, and the fee goes. */
    @Transaction
    suspend fun unlink(link: TransferLinkEntity, undoneAt: Instant) {
        getTransaction(link.debitId)?.let {
            updateTransaction(
                it.copy(type = TransactionType.EXPENSE, amount = link.debitAmount, transferGroup = null),
            )
        }
        getTransaction(link.creditId)?.let {
            updateTransaction(it.copy(type = TransactionType.INCOME, transferGroup = null))
        }
        link.feeId?.let { deleteTransaction(it) }
        updateLink(link.copy(undoneAt = undoneAt))
    }

    @Query("SELECT * FROM transfer_links WHERE id = :id")
    suspend fun getLink(id: Long): TransferLinkEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): TransactionEntity?

    /** Inserts [transaction] and returns its generated id. */
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Insert
    suspend fun insertLink(link: TransferLinkEntity): Long

    @Update
    suspend fun updateLink(link: TransferLinkEntity)
}
