package com.cauri.core.capture.signal

import com.cauri.core.data.model.Account
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.RawSignal
import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import com.cauri.core.parsing.model.ParsedMessage
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [DetectionService]. */
@Singleton
internal class DetectionServiceImpl @Inject constructor() : DetectionService {
    override fun detect(signal: RawSignal, parsed: ParsedMessage, account: Account): DetectedOperation {
        val foreign = parsed.currency != account.currency
        val type = when {
            parsed.kind == MessageKind.WITHDRAWAL -> DetectedType.WITHDRAWAL
            parsed.kind == MessageKind.DEPOSIT -> DetectedType.DEPOSIT
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
            counterparty = parsed.counterparty,
            occurredAt = parsed.occurredAt,
        )
    }
}
