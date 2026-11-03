package com.cauri.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cauri.core.data.database.entity.DecisionOutcome
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionStatus
import java.time.Instant

/** The answers of the user to the cards to confirm, each write all or none with its decision log. */
// Each answer and the queries it is made of, all in one transaction.
@Suppress("TooManyFunctions")
@Dao
internal interface ConfirmationDao {
    /**
     * Confirms the transaction [id], with [categoryId], [amount] and the [proposedCategoryId] next to it, and the other
     * side of its transfer; closes its open decisions as [outcome] at [at].
     */
    @Transaction
    @Suppress("LongParameterList") // What the card confirms, and when.
    suspend fun confirm(
        id: Long,
        categoryId: Long?,
        proposedCategoryId: Long?,
        amount: Long,
        outcome: DecisionOutcome,
        at: Instant,
    ) {
        updateAnswered(id = id, categoryId = categoryId, proposedCategoryId = proposedCategoryId, amount = amount)
        setTransferStatus(id = id, status = TransactionStatus.CONFIRMED)
        closeDecisions(transactionId = id, outcome = outcome, at = at)
    }

    /**
     * Confirms [transaction] as edited in the sheet, with the [parts] split from it in place of those of
     * [formerSplitGroup] and, for a transfer, the amount and time on its other side; closes its open decisions as
     * [outcome] at [at].
     */
    @Transaction
    suspend fun confirmEdited(
        transaction: TransactionEntity,
        formerSplitGroup: String?,
        parts: List<TransactionEntity>,
        outcome: DecisionOutcome,
        at: Instant,
    ) {
        formerSplitGroup?.let { deleteParts(splitGroup = it, keptId = transaction.id) }
        update(transaction)
        insertAll(parts)
        transaction.transferGroup?.let {
            updateOtherSide(
                id = transaction.id,
                transferGroup = it,
                amount = transaction.amount,
                occurredAt = transaction.occurredAt,
            )
        }
        setTransferStatus(id = transaction.id, status = TransactionStatus.CONFIRMED)
        closeDecisions(transactionId = transaction.id, outcome = outcome, at = at)
    }

    /** Sets the transaction [id] aside, with the other side of its transfer, and closes its decisions as [outcome]. */
    @Transaction
    suspend fun dismiss(id: Long, outcome: DecisionOutcome, at: Instant) {
        setTransferStatus(id = id, status = TransactionStatus.DISMISSED)
        closeDecisions(transactionId = id, outcome = outcome, at = at)
    }

    /** Puts the transaction [id] back to confirm, as read in its message, and opens its decisions again. */
    @Transaction
    suspend fun reopen(id: Long) {
        resetToProposal(id)
        setTransferStatus(id = id, status = TransactionStatus.DETECTED)
        openDecisions(id)
    }

    @Query("DELETE FROM transactions WHERE splitGroup = :splitGroup AND id != :keptId")
    suspend fun deleteParts(splitGroup: String, keptId: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): TransactionEntity?

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Insert
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query(
        "UPDATE transactions SET amount = :amount, occurredAt = :occurredAt WHERE transferGroup = :transferGroup " +
            "AND id != :id",
    )
    suspend fun updateOtherSide(id: Long, transferGroup: String, amount: Long, occurredAt: Instant)

    @Query(
        "UPDATE transactions SET categoryId = :categoryId, proposedCategoryId = :proposedCategoryId, " +
            "amount = :amount WHERE id = :id",
    )
    suspend fun updateAnswered(id: Long, categoryId: Long?, proposedCategoryId: Long?, amount: Long)

    @Query(
        "UPDATE transactions SET categoryId = NULL, amount = COALESCE(proposedAmount, amount) WHERE id = :id",
    )
    suspend fun resetToProposal(id: Long)

    /** Sets [status] on the transaction [id] and, when it is a side of a transfer, on the other side. */
    @Query(
        "UPDATE transactions SET status = :status WHERE id = :id OR (transferGroup IS NOT NULL AND transferGroup = " +
            "(SELECT transferGroup FROM transactions WHERE id = :id))",
    )
    suspend fun setTransferStatus(id: Long, status: TransactionStatus)

    /**
     * Closes the decisions about the transaction [transactionId] still open, or timed out: a late answer, such as from
     * the evening summary, corrects the outcome.
     */
    @Query(
        "UPDATE decision_log SET outcome = :outcome, outcomeAt = :at WHERE transactionId = :transactionId " +
            "AND (outcome IS NULL OR outcome = 'TIMEOUT')",
    )
    suspend fun closeDecisions(transactionId: Long, outcome: DecisionOutcome, at: Instant)

    @Query("UPDATE decision_log SET outcome = NULL, outcomeAt = NULL WHERE transactionId = :transactionId")
    suspend fun openDecisions(transactionId: Long)
}
