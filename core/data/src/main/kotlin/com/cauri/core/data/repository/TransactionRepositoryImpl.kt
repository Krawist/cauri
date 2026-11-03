package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.AccountDao
import com.cauri.core.data.database.dao.TransactionDao
import com.cauri.core.data.database.dao.TransactionMergeDao
import com.cauri.core.data.database.entity.AccountBalanceEntity
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionMergeEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.ManualEntry
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.ReconciledExpense
import com.cauri.core.data.model.RecordedEntry
import com.cauri.core.data.model.Withdrawal
import com.cauri.core.data.service.DuplicateService
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Default implementation for [TransactionRepository]. */
@Singleton
internal class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val mergeDao: TransactionMergeDao,
    accountDao: AccountDao,
    private val duplicates: DuplicateService,
    private val clock: Clock,
) : TransactionRepository {
    override val pending: Flow<List<PendingOperation>> = combine(
        transactionDao.observeByStatus(TransactionStatus.DETECTED),
        accountDao.observeAll(),
    ) { detected, accounts ->
        val names = accounts.associate { it.id to it.name }
        // A fee or the Cash side of a withdrawal has no signal of its own: it rides with the operation read.
        val (transfers, others) = detected.partition { it.transferGroup != null }
        val single = others.filter { it.rawSignalId != null }.map { it.toPending(names, to = null) }
        val paired = transfers.groupBy { it.transferGroup }.values.mapNotNull { sides ->
            val out = sides.firstOrNull { it.type == TransactionType.TRANSFER_OUT }
            val into = sides.firstOrNull { it.type == TransactionType.TRANSFER_IN }
            val read = sides.filter { it.rawSignalId != null }
            out?.takeIf { read.isNotEmpty() }?.let { side ->
                // One message names the agent of a withdrawal or a deposit; two messages, a transfer between the user's
                // accounts, name only the other account, already shown.
                side.copy(counterparty = read.singleOrNull()?.counterparty).toPending(names, to = into?.accountId)
            }
        }
        (single + paired).sortedByDescending { it.occurredAt }
    }

    override suspend fun recordWithdrawal(withdrawal: Withdrawal) {
        val parsed = withdrawal.rawSignalId != null
        val side = TransactionEntity(
            accountId = withdrawal.accountId,
            type = TransactionType.TRANSFER_OUT,
            status = if (parsed) TransactionStatus.DETECTED else TransactionStatus.CONFIRMED,
            origin = if (parsed) TransactionOrigin.SIGNAL else TransactionOrigin.MANUAL,
            amount = withdrawal.amount,
            occurredAt = withdrawal.occurredAt,
            categoryId = null,
            placeId = null,
            rawSignalId = null,
            transferGroup = UUID.randomUUID().toString(),
            counterparty = null,
            note = null,
            foreignAmount = null,
            foreignCurrency = null,
        )
        // The signal is kept on the side it comes from: a signal creates at most one transaction.
        val out = side.copy(rawSignalId = withdrawal.rawSignalId)
        val into = side.copy(accountId = CASH_ACCOUNT_ID, type = TransactionType.TRANSFER_IN)
        val fee = withdrawal.fee?.let { side.copy(type = TransactionType.EXPENSE, amount = it, transferGroup = null) }
        transactionDao.insertAll(listOfNotNull(out, into, fee))
    }

    override suspend fun recordDetected(operation: DetectedOperation) {
        val type = when (operation.type) {
            DetectedType.EXPENSE -> TransactionType.EXPENSE
            DetectedType.INCOME -> TransactionType.INCOME
            DetectedType.WITHDRAWAL -> TransactionType.TRANSFER_OUT
            DetectedType.DEPOSIT -> TransactionType.TRANSFER_IN
        }
        val transfer = type == TransactionType.TRANSFER_OUT || type == TransactionType.TRANSFER_IN
        val detected = TransactionEntity(
            accountId = operation.accountId,
            type = type,
            status = TransactionStatus.DETECTED,
            origin = TransactionOrigin.SIGNAL,
            amount = operation.amount,
            occurredAt = operation.occurredAt,
            categoryId = null,
            placeId = null,
            rawSignalId = operation.rawSignalId,
            transferGroup = if (transfer) UUID.randomUUID().toString() else null,
            counterparty = operation.counterparty,
            note = null,
            foreignAmount = operation.foreignAmount,
            foreignCurrency = operation.foreignCurrency,
        )
        // Only the detected side keeps the signal: a signal creates at most one transaction.
        val plain = detected.copy(rawSignalId = null, counterparty = null, foreignAmount = null, foreignCurrency = null)
        val cash = detected.transferGroup?.let {
            val cashType = if (type ==
                TransactionType.TRANSFER_OUT
            ) {
                TransactionType.TRANSFER_IN
            } else {
                TransactionType.TRANSFER_OUT
            }
            plain.copy(accountId = CASH_ACCOUNT_ID, type = cashType)
        }
        val fee = operation.fee?.let { plain.copy(type = TransactionType.EXPENSE, amount = it, transferGroup = null) }
        val manual = duplicates.sameOperation(
            transaction = detected,
            candidates = sameOperation(detected, status = TransactionStatus.CONFIRMED),
        )
        mergeDao.insertParsed(
            transactions = listOfNotNull(manual?.let { duplicates.merged(detected, it) } ?: detected, cash, fee),
            balance = operation.balance?.let {
                AccountBalanceEntity(
                    accountId = operation.accountId,
                    amount = it,
                    at = operation.occurredAt,
                    rawSignalId = operation.rawSignalId,
                )
            },
            rawSignalId = operation.rawSignalId,
            templateVersion = operation.templateVersion,
            entry = manual,
            // The detected transaction gets its id when inserted: the DAO fills it in.
            merge = manual?.let { mergeLog(kept = detected, mergedId = it.id) },
        )
    }

    override suspend fun recordEntry(entry: ManualEntry): RecordedEntry {
        val manual = TransactionEntity(
            accountId = entry.accountId,
            type = if (entry.type == EntryType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE,
            status = TransactionStatus.CONFIRMED,
            origin = TransactionOrigin.MANUAL,
            amount = entry.amount,
            occurredAt = entry.occurredAt,
            categoryId = entry.categoryId,
            placeId = null,
            rawSignalId = null,
            transferGroup = null,
            counterparty = null,
            note = entry.note,
            foreignAmount = null,
            foreignCurrency = null,
        )
        val detected = duplicates.sameOperation(
            transaction = manual,
            candidates = sameOperation(manual, status = TransactionStatus.DETECTED),
        )
        return if (detected != null) {
            val mergeId = mergeDao.insertMerged(
                entry = manual,
                kept = duplicates.merged(detected = detected, manual = manual),
                // The entry gets its id when inserted: the DAO fills it in.
                merge = mergeLog(kept = detected, mergedId = 0),
            )
            RecordedEntry(transactionId = detected.id, mergeId = mergeId)
        } else {
            RecordedEntry(transactionId = transactionDao.insert(manual), mergeId = null)
        }
    }

    override suspend fun undoMerge(mergeId: Long) {
        mergeDao.getMerge(mergeId)
            ?.takeIf { it.undoneAt == null }
            ?.let { mergeDao.undoMerge(merge = it, undoneAt = clock.instant()) }
    }

    // The transactions of the same account around [transaction], with [status]: the service picks the same operation.
    private suspend fun sameOperation(transaction: TransactionEntity, status: TransactionStatus) =
        transactionDao.getAround(
            accountId = transaction.accountId,
            status = status,
            from = transaction.occurredAt - DuplicateService.WINDOW,
            to = transaction.occurredAt + DuplicateService.WINDOW,
        )

    // The log of a merge into [kept], with the values [kept] had before.
    private fun mergeLog(kept: TransactionEntity, mergedId: Long) = TransactionMergeEntity(
        keptId = kept.id,
        mergedId = mergedId,
        mergedAt = clock.instant(),
        keptStatus = kept.status,
        keptCategoryId = kept.categoryId,
        keptNote = kept.note,
        keptPlaceId = kept.placeId,
        undoneAt = null,
    )

    override suspend fun recordCashExpense(amount: Long, categoryId: Long?, note: String, occurredAt: Instant): Long =
        transactionDao.insert(
            TransactionEntity(
                accountId = CASH_ACCOUNT_ID,
                type = TransactionType.EXPENSE,
                status = TransactionStatus.CONFIRMED,
                origin = TransactionOrigin.MANUAL,
                amount = amount,
                occurredAt = occurredAt,
                categoryId = categoryId,
                placeId = null,
                rawSignalId = null,
                transferGroup = null,
                counterparty = null,
                note = note,
                foreignAmount = null,
                foreignCurrency = null,
            ),
        )

    override suspend fun recordReconciliation(expenses: List<ReconciledExpense>, countedAt: Instant) {
        transactionDao.insertAll(
            expenses.map {
                TransactionEntity(
                    accountId = CASH_ACCOUNT_ID,
                    type = TransactionType.EXPENSE,
                    status = TransactionStatus.CONFIRMED,
                    origin = TransactionOrigin.RECONCILIATION,
                    amount = it.amount,
                    occurredAt = countedAt,
                    categoryId = it.categoryId,
                    placeId = null,
                    rawSignalId = null,
                    transferGroup = null,
                    counterparty = null,
                    note = it.note,
                    foreignAmount = null,
                    foreignCurrency = null,
                )
            },
        )
    }

    override suspend fun hasCashWithdrawalSince(since: Instant): Boolean = transactionDao.existsOnAccountAfter(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.TRANSFER_IN,
        after = since,
        excluded = TransactionStatus.NOT_COUNTED,
    )

    override suspend fun deleteTransaction(id: Long) = transactionDao.delete(id)
}

// The card of [this], an operation read in a message, with the names of its accounts.
private fun TransactionEntity.toPending(names: Map<Long, String>, to: Long?) = PendingOperation(
    transactionId = id,
    type = when (type) {
        TransactionType.EXPENSE -> PendingType.EXPENSE
        TransactionType.INCOME -> PendingType.INCOME
        TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> PendingType.TRANSFER
    },
    amount = amount,
    foreignAmount = foreignAmount,
    foreignCurrency = foreignCurrency,
    accountName = names.getValue(accountId),
    toAccountName = to?.let(names::getValue),
    counterparty = counterparty,
    occurredAt = occurredAt,
)
