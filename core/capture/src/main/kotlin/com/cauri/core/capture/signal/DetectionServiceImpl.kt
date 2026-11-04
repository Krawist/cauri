package com.cauri.core.capture.signal

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.RawSignal
import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import com.cauri.core.parsing.model.ParsedMessage
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [DetectionService]. */
@Singleton
internal class DetectionServiceImpl @Inject constructor() : DetectionService {
    override fun detect(
        signal: RawSignal,
        parsed: ParsedMessage,
        account: Account,
        accounts: List<Account>,
    ): DetectedOperation {
        val foreign = parsed.currency != account.currency
        val counterpart = counterpart(parsed, account, accounts)
        val type = when {
            parsed.kind == MessageKind.WITHDRAWAL -> DetectedType.WITHDRAWAL
            parsed.kind == MessageKind.DEPOSIT -> DetectedType.DEPOSIT
            counterpart != null && parsed.direction == Direction.DEBIT -> DetectedType.TRANSFER_OUT
            counterpart != null -> DetectedType.TRANSFER_IN
            parsed.direction == Direction.DEBIT -> DetectedType.EXPENSE
            else -> DetectedType.INCOME
        }
        return DetectedOperation(
            rawSignalId = signal.id,
            templateVersion = "${parsed.templateId}@${parsed.templateVersion}",
            accountId = account.id,
            type = type,
            amount = if (foreign) 0 else parsed.amount,
            fee = parsed.fees?.takeIf { !foreign && it > 0 },
            foreignAmount = parsed.amount.takeIf { foreign },
            foreignCurrency = parsed.currency.takeIf { foreign },
            balance = parsed.balance.takeIf { !foreign },
            counterparty = parsed.counterparty,
            occurredAt = parsed.occurredAt,
            counterpartAccountId = counterpart?.id,
        )
    }

    // The user's other account a transfer between their accounts names: « ORANGEMONEYCM » is Orange Money, « UBA UBA
    // 221--407 » is UBA. Never the Cash account, nor [account] itself; `null` for any other message.
    private fun counterpart(parsed: ParsedMessage, account: Account, accounts: List<Account>): Account? {
        val named = parsed.counterparty?.takeIf { parsed.ownAccount }?.let(::plain) ?: return null
        return accounts.firstOrNull { other ->
            other.id != account.id &&
                other.kind != AccountKind.CASH &&
                (plain(other.name) in named || plain(other.provider) in named)
        }
    }

    // Lower case, without accents, spaces nor underscores: « Orange Money » and « ORANGE_MONEY » are « orangemoney ».
    private fun plain(text: String): String = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        .filter { it.isLetterOrDigit() }
}
