package com.cauri.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.format.formatMoment
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

private val IconBoxSize = 40.dp

/**
 * « À confirmer » and its count, then one card per operation Cauri read in a message, newest first. The visits to
 * confirm come before them with the places (CAU-33+); ✓, the swipes and the proposed category with CAU-27.
 */
@Composable
internal fun PendingSection(pending: List<PendingOperation>) {
    if (pending.isEmpty()) return
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.padding(top = CauriSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_pending_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.ink,
        )
        Text(
            text = pending.size.toString(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.accentText,
            modifier = Modifier
                .background(palette.accentSoft, RoundedCornerShape(CauriRadius.field))
                .padding(horizontal = CauriSpacing.s, vertical = 2.dp),
        )
    }
    pending.forEach { PendingCard(it) }
}

@Composable
private fun PendingCard(operation: PendingOperation) {
    val palette = CauriTheme.palette
    val (icon, tint, background) = when (operation.type) {
        PendingType.EXPENSE -> Triple(R.drawable.ic_expense, palette.coralText, palette.coralSoft)
        PendingType.INCOME -> Triple(R.drawable.ic_income, palette.greenText, palette.greenSoft)
        PendingType.TRANSFER -> Triple(R.drawable.ic_transfer, palette.ink, palette.chip)
    }
    GlassSurface(shape = RoundedCornerShape(CauriRadius.card), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(CauriSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(IconBoxSize).background(background, RoundedCornerShape(CauriRadius.icon)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = tint)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = amount(operation),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                )
                Text(
                    text = details(operation),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// « 5 000 F », or the amount in its own currency for a payment abroad, until the user types what it cost.
@Composable
private fun amount(operation: PendingOperation): String {
    val foreign = operation.foreignAmount
    val currency = operation.foreignCurrency
    if (foreign == null || currency == null) return stringResource(R.string.home_amount, formatFcfa(operation.amount))
    val digits = Currency.getInstance(currency).defaultFractionDigits.coerceAtLeast(0)
    val format = NumberFormat.getNumberInstance(LocalConfiguration.current.locales[0]).apply {
        minimumFractionDigits = digits
        maximumFractionDigits = digits
    }
    return stringResource(R.string.home_foreign_amount, format.format(BigDecimal.valueOf(foreign, digits)), currency)
}

// « Boulangerie Saker · MTN MoMo · aujourd'hui à 8 h 30 », the transfer naming both accounts.
@Composable
private fun details(operation: PendingOperation): String {
    val account = operation.toAccountName?.let {
        stringResource(R.string.home_pending_transfer, operation.accountName, it)
    } ?: operation.accountName
    return listOfNotNull(operation.counterparty, account, formatMoment(operation.occurredAt))
        .joinToString(separator = stringResource(R.string.home_separator))
}
