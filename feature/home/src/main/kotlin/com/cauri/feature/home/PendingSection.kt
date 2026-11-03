package com.cauri.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.PendingType
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.UndoBar
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private const val SWIPE_THRESHOLD = 0.75f

// As the sections of the home screen.
private val CardGap = 14.dp
private val ChipHeight = 36.dp
private val SelectedBorderWidth = 1.5.dp

// Up to 10 000 000 F, as the quick entry.
private const val MAX_AMOUNT = 10_000_000L
private const val MAX_AMOUNT_DIGITS = 8

/**
 * « À confirmer » and its count, then one card per operation Cauri read in a message, newest first, pre-filled with its
 * proposal. ✓ or a swipe right confirms, a swipe left sets aside (« Rien dépensé »), a tap on the amount edits it. The
 * visits come first with the places (CAU-33+); a tap on the card opens the edit sheet with CAU-28.
 */
@Composable
internal fun PendingSection(viewModel: ConfirmationViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.cards.isEmpty()) return
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
            text = state.cards.size.toString(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.accentText,
            modifier = Modifier
                .background(palette.accentSoft, RoundedCornerShape(CauriRadius.field))
                .padding(horizontal = CauriSpacing.s, vertical = 2.dp),
        )
    }
    // The list closes smoothly over a card that left.
    Column(modifier = Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(CardGap)) {
        state.cards.forEach { card ->
            // Keyed by operation: a swipe belongs to its card, whatever leaves the list.
            key(card.operation.transactionId) { PendingCard(card = card, viewModel = viewModel) }
        }
    }
    state.cards.firstOrNull { it.operation.transactionId == state.editingAmount }?.let { card ->
        AmountDialog(
            amount = card.amount,
            onAmount = viewModel::onAmount,
            onDismiss = { viewModel.onEditAmount(null) },
        )
    }
}

/** « Confirmé » or « Mis de côté » with « Annuler », for a few seconds after a card is answered. */
@Composable
internal fun ConfirmationUndoBar(modifier: Modifier = Modifier, viewModel: ConfirmationViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    state.answered?.let { answered ->
        UndoBar(
            message = stringResource(
                if (answered.confirmed) R.string.home_card_confirmed else R.string.home_card_set_aside,
            ),
            onUndo = viewModel::onUndo,
            onDismissed = viewModel::onUndoShown,
            modifier = modifier,
        )
    }
}

// The two swipe directions never do the same thing: right confirms, left sets aside. The answer is given once the card
// has left the screen, and a tick is felt as the finger crosses the point where letting go answers.
@Composable
private fun PendingCard(card: ConfirmationCard, viewModel: ConfirmationViewModel) {
    val id = card.operation.transactionId
    val haptic = LocalHapticFeedback.current
    val swipe = rememberSwipeToDismissBoxState(
        // Three quarters of the way: a card is never answered by a slip of the finger.
        positionalThreshold = { distance -> distance * SWIPE_THRESHOLD },
        confirmValueChange = { value ->
            // A card still without amount comes back: it asks for the amount first.
            val asksAmount = value == SwipeToDismissBoxValue.StartToEnd && card.amount <= 0
            if (asksAmount) viewModel.onEditAmount(id)
            !asksAmount
        },
    )
    LaunchedEffect(swipe.targetValue) {
        if (swipe.targetValue != SwipeToDismissBoxValue.Settled) {
            haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        }
    }
    LaunchedEffect(swipe.currentValue) {
        when (swipe.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> viewModel.onConfirm(id)
            SwipeToDismissBoxValue.EndToStart -> viewModel.onDismiss(id)
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }
    SwipeToDismissBox(
        state = swipe,
        backgroundContent = {
            SwipeBackground(
                direction = swipe.dismissDirection,
                type = card.operation.type,
                // The colour comes in as the card goes: full once letting go would answer.
                progress = { if (swipe.targetValue == SwipeToDismissBoxValue.Settled) swipe.progress else 1f },
            )
        },
    ) {
        GlassSurface(shape = RoundedCornerShape(CauriRadius.card), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(CauriSpacing.l),
                verticalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
            ) {
                CardHeader(card = card, onEditAmount = {
                    viewModel.onEditAmount(id)
                }, onConfirm = { viewModel.onConfirm(id) })
                if (card.suggested.isNotEmpty()) {
                    CategoryChips(card = card, onSelect = { viewModel.onCategory(transactionId = id, categoryId = it) })
                }
            }
        }
    }
}

// The categories Cauri proposes, the chosen one outlined; a second tap leaves the operation without category.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(card: ConfirmationCard, onSelect: (Long) -> Unit) {
    val palette = CauriTheme.palette
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        card.suggested.forEach { category ->
            val chosen = category.id == card.categoryId
            Box(
                modifier = Modifier
                    .height(ChipHeight)
                    .clip(CircleShape)
                    .background(if (chosen) palette.accentSoft else palette.chip, CircleShape)
                    .then(if (chosen) Modifier.border(SelectedBorderWidth, palette.accent, CircleShape) else Modifier)
                    .selectable(selected = chosen, role = Role.RadioButton, onClick = { onSelect(category.id) })
                    .padding(horizontal = CauriSpacing.m + 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (chosen) palette.accentText else palette.ink,
                )
            }
        }
    }
}

// What a swipe does, shown under the card: « Valider » to the right, « Rien dépensé » or « Ignorer » to the left.
@Composable
private fun SwipeBackground(direction: SwipeToDismissBoxValue, type: PendingType, progress: () -> Float) {
    val palette = CauriTheme.palette
    val confirm = direction == SwipeToDismissBoxValue.StartToEnd
    if (direction == SwipeToDismissBoxValue.Settled) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = progress().coerceIn(0f, 1f) }
            .background(if (confirm) palette.accentSoft else palette.coralSoft, RoundedCornerShape(CauriRadius.card))
            .padding(horizontal = CauriSpacing.xl),
        contentAlignment = if (confirm) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Text(
            text = stringResource(
                when {
                    confirm -> R.string.home_card_confirm
                    type == PendingType.EXPENSE -> R.string.home_card_nothing_spent
                    else -> R.string.home_card_ignore
                },
            ),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (confirm) palette.accentText else palette.coralText,
        )
    }
}

@Composable
private fun AmountDialog(amount: Long, onAmount: (Long) -> Unit, onDismiss: () -> Unit) {
    val palette = CauriTheme.palette
    var digits by remember { mutableStateOf(amount.takeIf { it > 0 }?.toString().orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.background,
        title = { Text(text = stringResource(R.string.home_card_edit_amount)) },
        text = {
            CauriTextField(
                value = digits,
                onValueChange = { digits = it.filter(Char::isDigit).take(MAX_AMOUNT_DIGITS) },
                missing = false,
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = FcfaVisualTransformation,
                autoFocus = true,
                trailing = { Text(text = stringResource(R.string.home_currency), color = palette.muted) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { digits.toLongOrNull()?.takeIf { it in 1..MAX_AMOUNT }?.let(onAmount) },
            ) { Text(text = stringResource(R.string.home_card_amount_ok), color = palette.accentText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(DesignR.string.undo), color = palette.muted)
            }
        },
    )
}
