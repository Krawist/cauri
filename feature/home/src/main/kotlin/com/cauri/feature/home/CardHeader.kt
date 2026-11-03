package com.cauri.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.format.formatMoment
import com.cauri.core.design.format.readableName
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

private val AccountDotSize = 8.dp
private val IconBoxSize = 40.dp

// What happened in bold, the signed amount on the right, then the account, the kind of operation and when.
@Composable
internal fun CardHeader(card: ConfirmationCard, onEditAmount: () -> Unit, onConfirm: () -> Unit) {
    val palette = CauriTheme.palette
    val operation = card.operation
    val visit = operation.stayMinutes != null
    val (icon, tint, background) = when {
        // A shop visited: its gold pin, as the places.
        visit -> Triple(DesignR.drawable.ic_place, palette.goldText, palette.goldSoft)
        operation.type == PendingType.EXPENSE -> Triple(
            DesignR.drawable.ic_expense,
            palette.coralText,
            palette.coralSoft,
        )
        operation.type == PendingType.INCOME -> Triple(DesignR.drawable.ic_income, palette.greenText, palette.greenSoft)
        else -> Triple(DesignR.drawable.ic_transfer, palette.ink, palette.chip)
    }
    val kind = operation.stayMinutes?.let { stringResource(R.string.home_card_kind_visit, it) }
        ?: stringResource(kindLabel(operation))
    val title = operation.toAccountName?.let {
        stringResource(R.string.home_pending_transfer, operation.accountName, it)
    } ?: operation.counterparty?.let(::readableName) ?: kind
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(IconBoxSize).background(background, RoundedCornerShape(CauriRadius.icon)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = tint)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = CauriSpacing.s),
                )
                Amount(
                    card = card,
                    tint = when {
                        operation.type == PendingType.TRANSFER -> palette.ink
                        // A visit spends what the user says: coral as any expense.
                        visit -> palette.coralText
                        else -> tint
                    },
                    onEditAmount,
                )
            }
            Details(operation = operation, kind = kind.takeIf { it != title })
        }
        ConfirmButton(onConfirm = onConfirm)
    }
}

// ✓: confirms the card as Cauri proposed it, felt as a confirmation.
@Composable
private fun ConfirmButton(onConfirm: () -> Unit) {
    val palette = CauriTheme.palette
    val haptics = rememberCauriHaptics()
    FilledIconButton(
        onClick = {
            haptics.confirm()
            onConfirm()
        },
        modifier = Modifier.size(IconBoxSize),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = palette.accent,
            contentColor = palette.onAccent,
        ),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_check),
            contentDescription = stringResource(R.string.home_card_confirm),
        )
    }
}

// « − 1 500 F » in coral, « + 25 000 F » in green, a transfer without sign; a tap types another amount.
@Composable
private fun Amount(card: ConfirmationCard, tint: Color, onEditAmount: () -> Unit) {
    val amount = amount(card)
    Text(
        text = when (card.operation.type) {
            PendingType.EXPENSE -> stringResource(R.string.home_sign_out, amount)
            PendingType.INCOME -> stringResource(R.string.home_sign_in, amount)
            PendingType.TRANSFER -> amount
        },
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = tint,
        maxLines = 1,
        modifier = Modifier.clickable(
            onClickLabel = stringResource(R.string.home_card_edit_amount),
            role = Role.Button,
            onClick = onEditAmount,
        ),
    )
}

// « ● Orange Money · Paiement · aujourd'hui à 18 h 02 »; a transfer, its accounts in the title, starts with its kind.
@Composable
private fun Details(operation: PendingOperation, kind: String?) {
    val palette = CauriTheme.palette
    val transfer = operation.toAccountName != null
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!transfer) {
            Box(
                modifier = Modifier
                    .padding(end = CauriSpacing.xs)
                    .size(AccountDotSize)
                    .background(accountColor(operation.accountKind), CircleShape),
            )
            Text(
                text = operation.accountName,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = palette.ink,
                maxLines = 1,
            )
        }
        val rest = listOfNotNull(kind, operation.counterparty?.takeIf { transfer }?.let(::readableName))
            .plus(formatMoment(operation.occurredAt))
            .joinToString(separator = stringResource(R.string.home_separator))
        Text(
            text = if (transfer) rest else stringResource(R.string.home_separator) + rest,
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// Gold for cash, as everywhere; lagoon for mobile money and its cards, ink for a bank.
@Composable
private fun accountColor(kind: AccountKind) = when (kind) {
    AccountKind.CASH -> CauriTheme.palette.gold
    AccountKind.MOBILE_MONEY, AccountKind.PREPAID_CARD -> CauriTheme.palette.accent
    AccountKind.BANK -> CauriTheme.palette.ink
}

// What the operation is, in words: a transfer from or to Cash is a withdrawal or a deposit.
private fun kindLabel(operation: PendingOperation) = when {
    operation.type == PendingType.EXPENSE -> R.string.home_card_kind_payment
    operation.type == PendingType.INCOME -> R.string.home_card_kind_income
    operation.toAccountKind == AccountKind.CASH -> R.string.home_card_kind_withdrawal
    operation.accountKind == AccountKind.CASH -> R.string.home_card_kind_deposit
    else -> R.string.home_card_kind_transfer
}

// « 5 000 F », fees included, or the amount in its own currency for a payment abroad, until the user types what it
// cost; « Montant ? » for a shop visited with no usual amount yet.
@Composable
private fun amount(card: ConfirmationCard): String {
    val foreign = card.operation.foreignAmount
    val currency = card.operation.foreignCurrency
    if (card.amount > 0 || foreign == null || currency == null) {
        // What left the account: the fees of its message included.
        return if (card.amount > 0 || foreign != null) {
            stringResource(R.string.home_amount, formatFcfa(card.amount + card.operation.fees))
        } else {
            stringResource(R.string.home_card_amount_to_type)
        }
    }
    val digits = Currency.getInstance(currency).defaultFractionDigits.coerceAtLeast(0)
    val format = NumberFormat.getNumberInstance(LocalConfiguration.current.locales[0]).apply {
        minimumFractionDigits = digits
        maximumFractionDigits = digits
    }
    return stringResource(R.string.home_foreign_amount, format.format(BigDecimal.valueOf(foreign, digits)), currency)
}
