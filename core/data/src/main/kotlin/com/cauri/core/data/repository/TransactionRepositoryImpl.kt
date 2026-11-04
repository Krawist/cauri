package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.AccountDao
import com.cauri.core.data.database.dao.TransactionDao
import com.cauri.core.data.database.dao.TransactionMergeDao
import com.cauri.core.data.database.dao.TransferLinkDao
import com.cauri.core.data.database.dao.VisitDao
import com.cauri.core.data.database.entity.AccountBalanceEntity
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionMergeEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.EditableTransaction
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.ManualEntry
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.ReconciledExpense
import com.cauri.core.data.model.RecordedEntry
import com.cauri.core.data.model.SplitPart
import com.cauri.core.data.model.Withdrawal
import com.cauri.core.data.service.BalanceService
import com.cauri.core.data.service.DuplicateService
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Default implementation for [TransactionRepository]. */
// One function per way the money moves on the accounts, and the edit sheet's read; one DAO per table it reads.
@Suppress("TooManyFunctions", "LongParameterList")
@Singleton
internal class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val mergeDao: TransactionMergeDao,
    private val transferLinkDao: TransferLinkDao,
    accountDao: AccountDao,
    visitDao: VisitDao,
    private val duplicates: DuplicateService,
    private val balances: BalanceService,
    private val clock: Clock,
) : TransactionRepository {
    override val pending: Flow<List<PendingOperation>> = combine(
        transactionDao.observeByStatus(TransactionStatus.DETECTED),
        accountDao.observeAll(),
        visitDao.observeWithCard(),
        transactionDao.observeReadByModel(),
    ) { detected, accounts, visits, readByModel ->
        val byModel = readByModel.toSet()
        val byId = accounts.associateBy { it.id }
        // How long each visit with a card lasted, by its card.
        val stays = visits.associate { visit ->
            visit.transactionId to visit.leftAt?.let { Duration.between(visit.enteredAt, it).toMinutes() }
        }
        // A fee or the Cash side of a withdrawal has no signal of its own: it rides with the operation read. A fee is
        // on its account and at its time.
        val (transfers, others) = detected.partition { it.transferGroup != null }
        val fees = others.filter {
            it.rawSignalId == null && it.type == TransactionType.EXPENSE && it.origin != TransactionOrigin.VISIT
        }
            .groupBy({ it.accountId to it.occurredAt }, { it.amount })
            .mapValues { (_, amounts) -> amounts.sum() }
        fun feesOf(vararg sides: TransactionEntity?) =
            sides.filterNotNull().sumOf { fees[it.accountId to it.occurredAt] ?: 0 }
        val single = others.filter { it.rawSignalId != null }.map {
            it.toPending(byId, to = null, fees = if (it.type == TransactionType.EXPENSE) feesOf(it) else 0)
                .copy(readByModel = it.id in byModel)
        }
        // A shop left with nothing paid: what was spent there in cash, the amount to type.
        val visited = others.filter { it.origin == TransactionOrigin.VISIT }.map {
            it.toPending(byId, to = null, fees = 0).copy(stayMinutes = stays[it.id])
        }
        val paired = transfers.groupBy { it.transferGroup }.values.mapNotNull { sides ->
            val out = sides.firstOrNull { it.type == TransactionType.TRANSFER_OUT }
            val into = sides.firstOrNull { it.type == TransactionType.TRANSFER_IN }
            val read = sides.filter { it.rawSignalId != null }
            out?.takeIf { read.isNotEmpty() }?.let { side ->
                // One message names the agent of a withdrawal or a deposit; two messages, a transfer between the user's
                // accounts, name only the other account, already shown.
                side.copy(counterparty = read.singleOrNull()?.counterparty)
                    .toPending(byId, to = into?.accountId, fees = feesOf(out, into))
                    .copy(readByModel = read.any { it.id in byModel })
            }
        }
        (single + visited + paired).sortedByDescending { it.occurredAt }
    }

    override suspend fun getEditable(transactionId: Long): EditableTransaction? {
        val transaction = transactionDao.getById(transactionId) ?: return null
        val sides = transaction.transferGroup?.let { transactionDao.getByTransferGroup(it) } ?: listOf(transaction)
        val parts = transaction.splitGroup?.let { transactionDao.getBySplitGroup(it) }.orEmpty()
            .filter { it.id != transaction.id }
        return EditableTransaction(
            transactionId = transaction.id,
            type = transaction.type.toPendingType(),
            amount = transaction.amount + parts.sumOf { it.amount },
            foreignAmount = transaction.foreignAmount,
            foreignCurrency = transaction.foreignCurrency,
            accountId = transaction.accountId,
            toAccountId = sides.firstOrNull { it.type == TransactionType.TRANSFER_IN && it.id != transaction.id }
                ?.accountId,
            categoryId = transaction.categoryId,
            parts = parts.mapNotNull { part ->
                part.categoryId?.let { SplitPart(categoryId = it, amount = part.amount) }
            },
            counterparty = sides.firstNotNullOfOrNull { it.counterparty },
            note = transaction.note,
            occurredAt = transaction.occurredAt,
            // A side of a withdrawal or a deposit has no signal of its own: the message is on the other side.
            read = sides.any { it.rawSignalId != null },
            pending = transaction.status == TransactionStatus.DETECTED,
            mergeId = mergeDao.getHeldMergeId(transaction.id),
            transferLinkId = transferLinkDao.getHeldLinkId(sides.map { it.id }),
            proposedAmount = transaction.proposedAmount.takeIf { transaction.amount == 0L },
        )
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

    // The balance the message gave after [operation], or the one before it moved by the operation when the bank forgot
    // to update it (BalanceService); `null` when the message gave none.
    private suspend fun balanceAfter(operation: DetectedOperation) = operation.balance?.let { read ->
        AccountBalanceEntity(
            accountId = operation.accountId,
            amount = balances.afterOperation(
                read = read,
                previous = mergeDao.getBalanceAt(accountId = operation.accountId, at = operation.occurredAt),
                change = operation.change(),
            ),
            at = operation.occurredAt,
            rawSignalId = operation.rawSignalId,
        )
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
            // The amount read is Cauri's proposal: the user may correct it when confirming.
            proposedAmount = operation.amount.takeIf { operation.foreignCurrency == null },
        )
        // Only the detected side keeps the signal: a signal creates at most one transaction.
        val plain = detected.copy(
            rawSignalId = null,
            counterparty = null,
            foreignAmount = null,
            foreignCurrency = null,
            proposedAmount = null,
        )
        // The Cash side goes the other way: a withdrawal arrives on Cash, a deposit leaves it.
        val cash = detected.transferGroup?.let {
            plain.copy(
                accountId = CASH_ACCOUNT_ID,
                type = if (type == TransactionType.TRANSFER_OUT) {
                    TransactionType.TRANSFER_IN
                } else {
                    TransactionType.TRANSFER_OUT
                },
            )
        }
        val fee = operation.fee?.let { plain.copy(type = TransactionType.EXPENSE, amount = it, transferGroup = null) }
        val manual = duplicates.sameOperation(
            transaction = detected,
            candidates = sameOperation(detected, status = TransactionStatus.CONFIRMED),
        )
        mergeDao.insertParsed(
            transactions = listOfNotNull(manual?.let { duplicates.merged(detected, it) } ?: detected, cash, fee),
            balance = balanceAfter(operation),
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
            origin = if (entry.fromFreeText) TransactionOrigin.NATURAL_LANGUAGE else TransactionOrigin.MANUAL,
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
            proposedCategoryId = entry.proposedCategoryId,
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

    override suspend fun recordReconciliation(accountId: Long, expenses: List<ReconciledExpense>, at: Instant) {
        transactionDao.insertAll(
            expenses.map {
                TransactionEntity(
                    accountId = accountId,
                    type = TransactionType.EXPENSE,
                    status = TransactionStatus.CONFIRMED,
                    origin = TransactionOrigin.RECONCILIATION,
                    amount = it.amount,
                    occurredAt = at,
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

// The card of [this], an operation read in a message, with the names and kinds of its accounts and its [fees].
private fun TransactionEntity.toPending(accounts: Map<Long, AccountEntity>, to: Long?, fees: Long) = PendingOperation(
    transactionId = id,
    accountId = accountId,
    type = type.toPendingType(),
    amount = amount,
    fees = fees,
    foreignAmount = foreignAmount,
    foreignCurrency = foreignCurrency,
    accountName = accounts.getValue(accountId).name,
    accountKind = accounts.getValue(accountId).kind,
    toAccountName = to?.let { accounts.getValue(it).name },
    toAccountKind = to?.let { accounts.getValue(it).kind },
    counterparty = counterparty,
    occurredAt = occurredAt,
    categoryId = categoryId,
    proposedAmount = proposedAmount.takeIf { amount == 0L },
    placeId = placeId,
)

private fun TransactionType.toPendingType() = when (this) {
    TransactionType.EXPENSE -> PendingType.EXPENSE
    TransactionType.INCOME -> PendingType.INCOME
    TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> PendingType.TRANSFER
}

// What [this] moved on its account, fees included: positive when money arrived.
private fun DetectedOperation.change() = when (type) {
    DetectedType.INCOME, DetectedType.DEPOSIT -> amount
    DetectedType.EXPENSE, DetectedType.WITHDRAWAL -> -(amount + (fee ?: 0))
}
