package com.cauri.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.dashedBorder
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.readableName
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private const val SWIPE_THRESHOLD = 0.75f
private val LineHeight = 72.dp
private val IconBoxSize = 40.dp
private val PillBorderWidth = 1.5.dp

/**
 * A line of « Ce soir »: what happened, how and where, the amount in a pill (« Montant ? » dashed while none), and ✓
 * once there is one. A swipe to the left sets it aside; a tap on the amount types another, on the line opens the sheet.
 */
@Composable
internal fun SummaryLine(card: ConfirmationCard, viewModel: ConfirmationViewModel, onEdit: (Long) -> Unit) {
    val id = card.operation.transactionId
    val haptics = rememberCauriHaptics()
    val swipe = rememberSwipeToDismissBoxState(positionalThreshold = { distance -> distance * SWIPE_THRESHOLD })
    LaunchedEffect(swipe.targetValue) {
        if (swipe.targetValue != SwipeToDismissBoxValue.Settled) haptics.threshold()
    }
    LaunchedEffect(swipe.currentValue) {
        if (swipe.currentValue == SwipeToDismissBoxValue.EndToStart) {
            haptics.reject()
            viewModel.onDismiss(id)
        }
    }
    SwipeToDismissBox(
        state = swipe,
        // Only « Rien dépensé » slides: ✓ confirms.
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            SwipeBackground(
                direction = swipe.dismissDirection,
                type = card.operation.type,
                progress = { if (swipe.targetValue == SwipeToDismissBoxValue.Settled) swipe.progress else 1f },
            )
        },
    ) {
        LineContent(card = card, viewModel = viewModel, onEdit = onEdit)
    }
}

@Composable
private fun LineContent(card: ConfirmationCard, viewModel: ConfirmationViewModel, onEdit: (Long) -> Unit) {
    val palette = CauriTheme.palette
    val operation = card.operation
    val id = operation.transactionId
    val kind = stringResource(kindLabel(operation))
    val title =
        operation.toAccountName?.let { stringResource(R.string.home_pending_transfer, operation.accountName, it) }
            ?: operation.counterparty?.let(::readableName)
            ?: kind
    // « Visite de 25 min · cash », else « Orange Money · Alimentation ».
    val details = operation.stayMinutes?.let { stringResource(R.string.home_summary_visit, it) }
        ?: listOf(
            operation.accountName,
            card.suggested.firstOrNull { it.id == card.categoryId }?.name ?: kind,
        ).joinToString(separator = stringResource(R.string.home_separator))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = LineHeight)
            .clickable(onClickLabel = stringResource(R.string.home_card_edit), role = Role.Button) { onEdit(id) }
            .padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LineIcon(operation = operation)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AmountPill(card = card, onClick = { viewModel.onEditAmount(id) })
        if (card.amount > 0) {
            ConfirmButton(onConfirm = { viewModel.onConfirm(id) })
        } else {
            Spacer(Modifier.size(IconBoxSize))
        }
    }
}

// A shop visited has its gold pin, as the places; an operation the icon of its type.
@Composable
private fun LineIcon(operation: PendingOperation) {
    val palette = CauriTheme.palette
    val (icon, tint, background) = when {
        operation.stayMinutes != null -> Triple(DesignR.drawable.ic_place, palette.goldText, palette.goldSoft)
        operation.type == PendingType.EXPENSE ->
            Triple(DesignR.drawable.ic_expense, palette.coralText, palette.coralSoft)
        operation.type == PendingType.INCOME -> Triple(DesignR.drawable.ic_income, palette.greenText, palette.greenSoft)
        else -> Triple(DesignR.drawable.ic_transfer, palette.ink, palette.chip)
    }
    Box(
        modifier = Modifier.size(IconBoxSize).background(background, RoundedCornerShape(CauriRadius.icon)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = tint)
    }
}

// The amount outlined in lagoon, or « Montant ? » dashed while there is none.
@Composable
private fun AmountPill(card: ConfirmationCard, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.field)
    val known = card.amount > 0
    Text(
        text = amount(card),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = if (known) FontWeight.Bold else FontWeight.SemiBold,
        ),
        color = if (known) palette.accentText else palette.muted,
        maxLines = 1,
        modifier = Modifier
            .then(
                if (known) {
                    Modifier.background(palette.accentSoft, shape).border(PillBorderWidth, palette.accent, shape)
                } else {
                    Modifier.dashedBorder(color = palette.handle, radius = CauriRadius.field)
                },
            )
            .clickable(
                onClickLabel = stringResource(R.string.home_card_edit_amount),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = CauriSpacing.m, vertical = CauriSpacing.s - 2.dp),
    )
}

@Composable
private fun ConfirmButton(onConfirm: () -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    FilledIconButton(
        onClick = {
            haptics.confirm()
            onConfirm()
        },
        modifier = Modifier.size(IconBoxSize),
        shape = CircleShape,
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
