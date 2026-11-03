package com.cauri.feature.transactions.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.PendingType
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.format.readableName
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R

private val RowHeight = 64.dp
private val AvatarSize = 38.dp
private val AvatarRadius = 13.dp
private val IconSize = 18.dp

/**
 * An operation: its avatar, what it was (« Moto-taxi », « Retrait au guichet ») over its category and account, and its
 * signed amount; « À confirmer » while it waits, struck through once set aside, grey for a transfer. A tap opens it.
 */
@Composable
internal fun OperationRow(line: ActivityLine.Operation, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    val title = title(line)
    val setAside = line.status == ActivityStatus.SET_ASIDE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CauriSpacing.l),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OperationAvatar(line = line, title = title)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Title(text = title, color = if (setAside) palette.muted else palette.ink)
            when (line.status) {
                ActivityStatus.PENDING -> Text(
                    text = stringResource(R.string.transactions_activity_pending),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.accentText,
                )
                ActivityStatus.SET_ASIDE -> Subtitle(text = stringResource(R.string.transactions_activity_set_aside))
                ActivityStatus.CONFIRMED -> Subtitle(text = subtitle(line = line, title = title))
            }
        }
        val amount = formatFcfa(line.amount)
        Text(
            text = when (line.type) {
                PendingType.EXPENSE -> stringResource(R.string.transactions_sign_out, amount)
                PendingType.INCOME -> stringResource(R.string.transactions_sign_in, amount)
                PendingType.TRANSFER -> amount
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (line.type == PendingType.TRANSFER) FontWeight.Medium else FontWeight.SemiBold,
                textDecoration = if (setAside) TextDecoration.LineThrough else null,
            ),
            // Coral for what left, green for what arrived; a transfer stays grey, never spending.
            color = when {
                setAside || line.type == PendingType.TRANSFER -> palette.muted
                line.type == PendingType.INCOME -> palette.greenText
                else -> palette.coralText
            },
        )
    }
}

/** A cash count: what was counted, and the gap with what Cauri expected. */
@Composable
internal fun CashCountRow(line: ActivityLine.CashCount) {
    val palette = CauriTheme.palette
    val gap = line.expectedAmount?.let { it - line.amount }
    Row(
        modifier = Modifier.fillMaxWidth().height(RowHeight).padding(horizontal = CauriSpacing.l),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(background = palette.goldSoft) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_count),
                contentDescription = null,
                tint = palette.goldText,
                modifier = Modifier.size(IconSize),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Title(text = stringResource(R.string.transactions_activity_count), color = palette.ink)
            Subtitle(
                text = when {
                    gap == null -> stringResource(R.string.transactions_activity_count_first)
                    gap > 0 -> pluralStringResource(
                        R.plurals.transactions_activity_count_gap,
                        line.expenses,
                        formatFcfa(gap),
                        line.expenses,
                    )
                    gap < 0 -> stringResource(R.string.transactions_activity_count_surplus, formatFcfa(-gap))
                    else -> stringResource(R.string.transactions_activity_count_even)
                },
            )
        }
        Text(
            text = formatFcfa(line.amount),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = palette.muted,
        )
    }
}

// An arrow for an income, the two arrows of a transfer, otherwise the initial: coral while it waits, lagoon for cash.
@Composable
private fun OperationAvatar(line: ActivityLine.Operation, title: String) {
    val palette = CauriTheme.palette
    val icon = when (line.type) {
        PendingType.INCOME -> Triple(DesignR.drawable.ic_income, palette.greenSoft, palette.greenText)
        PendingType.TRANSFER -> Triple(DesignR.drawable.ic_transfer, palette.chip, palette.muted)
        PendingType.EXPENSE -> null
    }
    if (icon != null && line.status != ActivityStatus.SET_ASIDE) {
        Avatar(background = icon.second) {
            Icon(
                painter = painterResource(icon.first),
                contentDescription = null,
                tint = icon.third,
                modifier = Modifier.size(IconSize),
            )
        }
        return
    }
    val (background, color) = when {
        line.status == ActivityStatus.SET_ASIDE -> palette.chip to palette.muted
        line.status == ActivityStatus.PENDING -> palette.coralSoft to palette.coralText
        line.accountKind == AccountKind.CASH -> palette.accentSoft to palette.accentText
        else -> palette.chip to palette.ink
    }
    Avatar(background = background) {
        Text(
            text = title.take(1).uppercase(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}

@Composable
private fun Avatar(background: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(AvatarSize).background(background, RoundedCornerShape(AvatarRadius)),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun Title(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun Subtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = CauriTheme.palette.muted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

// What the operation was: the note or the habit, the merchant, the category, else its kind.
@Composable
private fun title(line: ActivityLine.Operation): String = when {
    line.type == PendingType.TRANSFER -> stringResource(
        when {
            line.toAccountKind == AccountKind.CASH -> R.string.transactions_activity_withdrawal
            line.accountKind == AccountKind.CASH -> R.string.transactions_activity_deposit
            else -> R.string.transactions_type_transfer
        },
    )
    line.note != null -> line.note.orEmpty()
    line.counterparty != null -> readableName(line.counterparty.orEmpty())
    line.fee -> stringResource(R.string.transactions_activity_fee)
    line.categoryName != null -> line.categoryName.orEmpty()
    line.reconciled -> stringResource(R.string.transactions_activity_cash_misc)
    line.type == PendingType.INCOME -> stringResource(R.string.transactions_type_income)
    else -> stringResource(R.string.transactions_type_expense)
}

// « Transport · Cash », « Réparti en 2 · Orange Money », « MoMo → Cash » for a transfer, then « dont 150 F de frais ».
@Composable
private fun subtitle(line: ActivityLine.Operation, title: String): String {
    val toAccount = line.toAccountName
    // The amount shown includes them: the edit sheet shows the operation alone.
    val fees = line.fees.takeIf { it > 0 }?.let { stringResource(R.string.transactions_activity_fees, formatFcfa(it)) }
    val category = when {
        toAccount != null -> null
        line.parts > 1 -> stringResource(R.string.transactions_activity_split, line.parts)
        else -> line.categoryName?.takeIf { it != title }
    }
    val accounts = toAccount?.let { stringResource(R.string.transactions_transfer, line.accountName, it) }
        ?: line.accountName
    return listOfNotNull(category, accounts, fees)
        .joinToString(separator = stringResource(R.string.transactions_separator))
}
