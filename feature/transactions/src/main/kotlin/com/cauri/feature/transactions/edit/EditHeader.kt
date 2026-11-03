package com.cauri.feature.transactions.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Account
import com.cauri.core.data.model.EditableTransaction
import com.cauri.core.data.model.PendingType
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.format.readableName
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R

private val IconBoxSize = 48.dp
private val IconBoxRadius = 16.dp

/**
 * Where the transaction comes from and what it is, as in the mockup: « Lu dans un message » over the merchant, the
 * two accounts of a transfer, the note or the habit, or else its type.
 */
@Composable
internal fun EditHeader(transaction: EditableTransaction, accounts: List<Account>) {
    val palette = CauriTheme.palette
    val names = accounts.associate { it.id to it.name }
    val from = names[transaction.accountId]
    val to = transaction.toAccountId?.let(names::get)
    val title = when {
        from != null && to != null -> stringResource(R.string.transactions_transfer, from, to)
        transaction.counterparty != null -> readableName(transaction.counterparty.orEmpty())
        // What the user typed, or the name of a habit.
        transaction.note != null -> transaction.note.orEmpty()
        else -> stringResource(
            when (transaction.type) {
                PendingType.EXPENSE -> R.string.transactions_type_expense
                PendingType.INCOME -> R.string.transactions_type_income
                PendingType.TRANSFER -> R.string.transactions_type_transfer
            },
        )
    }
    // The message it was read in, else its type.
    val (icon, tint, background) = when {
        transaction.read -> Triple(DesignR.drawable.ic_message, palette.goldText, palette.goldSoft)
        transaction.type == PendingType.EXPENSE ->
            Triple(DesignR.drawable.ic_expense, palette.coralText, palette.coralSoft)
        transaction.type == PendingType.INCOME ->
            Triple(DesignR.drawable.ic_income, palette.greenText, palette.greenSoft)
        else -> Triple(DesignR.drawable.ic_transfer, palette.ink, palette.chip)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(IconBoxSize).background(background, RoundedCornerShape(IconBoxRadius)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = tint)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (transaction.read) {
                Text(
                    text = stringResource(R.string.transactions_edit_read),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
