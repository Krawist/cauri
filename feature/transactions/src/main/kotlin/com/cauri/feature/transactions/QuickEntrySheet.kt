package com.cauri.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.Category
import com.cauri.core.design.component.AnimatedPresence
import com.cauri.core.design.component.Keypad
import com.cauri.core.design.component.RollingNumber
import com.cauri.core.design.component.UndoBar
import com.cauri.core.design.component.backdrop
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.component.screenBackdropShapes
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassScreen
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.entry.EntryChip
import com.cauri.feature.transactions.entry.EntryChipHeight
import com.cauri.feature.transactions.entry.EntryHeader
import com.cauri.feature.transactions.entry.FreeTextEntry
import com.cauri.feature.transactions.entry.QuickEntryActions
import com.cauri.feature.transactions.entry.QuickEntryType
import com.cauri.feature.transactions.entry.QuickEntryUiState
import com.cauri.feature.transactions.entry.QuickEntryViewModel

private val AccountChipWidth = 72.dp

// Three lines of chips and the gaps between them.
private val CategoriesMaxHeight = EntryChipHeight * 3 + CauriSpacing.s * 2
private val SaveHeight = 56.dp
private val AmountSize = 56.sp
private val CurrencySize = 28.sp

/**
 * Quick entry sheet, opened by « + »: the type, the amount on a keypad, the account (Cash first), the most likely
 * categories and « Enregistrer », which records and closes it. Back, a swipe down or the cross closes it through
 * [onDismiss] without recording.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEntrySheet(onDismiss: () -> Unit) {
    val viewModel: QuickEntryViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = CauriTheme.palette
    LaunchedEffect(Unit) { viewModel.onOpen() }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.onSavedShown()
            onDismiss()
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.background,
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        // The sheet is its own window, out of reach of the screen's shapes: it is a glass screen of its own, whose keys
        // and chips frost the same shapes drawn behind them.
        GlassScreen(
            // As tall as what it holds: the sheet leaves the screen in sight above it.
            fillMaxSize = false,
            background = { Box(Modifier.fillMaxSize().backdrop(screenBackdropShapes())) },
        ) {
            QuickEntryContent(state = state, actions = viewModel, onClose = onDismiss)
        }
    }
}

/**
 * « Fusionné avec une opération reçue par message » with « Annuler », for a few seconds after the quick entry merged
 * what the user typed into an operation Cauri detected before.
 */
@Composable
fun QuickEntryMergeBar(modifier: Modifier = Modifier) {
    val viewModel: QuickEntryViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AnimatedPresence(
        value = state.mergeId,
        enter = CauriTheme.motion.barEnter(),
        exit = CauriTheme.motion.barExit(),
        modifier = modifier,
    ) {
        UndoBar(
            message = stringResource(R.string.transactions_merged),
            onUndo = viewModel::onUndoMerge,
            onDismissed = viewModel::onMergeShown,
        )
    }
}

@Composable
private fun QuickEntryContent(state: QuickEntryUiState, actions: QuickEntryActions, onClose: () -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // A small screen scrolls rather than cutting « Enregistrer ».
            .verticalScroll(rememberScrollState())
            .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.l),
    ) {
        EntryHeader(type = state.type, onType = actions::onType, onClose = onClose)
        // Several expenses at once in words, on the phones whose model reads them (CAU-50).
        if (state.type == QuickEntryType.EXPENSE) FreeTextEntry(accountId = state.accountId, onSaved = onClose)
        Amount(digits = state.amount, tooHigh = state.amountTooHigh)
        if (state.type == QuickEntryType.TRANSFER) {
            Label(text = stringResource(R.string.transactions_from))
        }
        AccountChips(accounts = state.accounts, selected = state.accountId, onSelect = actions::onAccount)
        if (state.type == QuickEntryType.TRANSFER) {
            Label(text = stringResource(R.string.transactions_to))
            AccountChips(
                accounts = state.accounts.filter { it.id != state.accountId },
                selected = state.toAccountId,
                onSelect = actions::onToAccount,
            )
        } else {
            CategoryChips(state = state, actions = actions)
        }
        Keypad(onDigits = actions::onDigits, onDelete = actions::onDelete, onClear = actions::onClear)
        Button(
            onClick = {
                haptics.confirm()
                actions.onSave()
            },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth().height(SaveHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(
                text = stringResource(R.string.transactions_save),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

// The amount, and under it why a digit was refused.
@Composable
private fun Amount(digits: String, tooHigh: Boolean) {
    val haptics = rememberCauriHaptics()
    // A digit refused over the largest amount is felt, not only read.
    LaunchedEffect(tooHigh) { if (tooHigh) haptics.reject() }
    val palette = CauriTheme.palette
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        AmountText(digits = digits)
        if (tooHigh) {
            Text(
                text = stringResource(
                    R.string.transactions_amount_too_high,
                    formatFcfa(QuickEntryViewModel.MAX_AMOUNT),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.coralText,
            )
        }
    }
}

// « 1 500 F », the currency smaller and muted; « 0 F » before the first digit.
@Composable
private fun AmountText(digits: String) {
    val palette = CauriTheme.palette
    val currency = stringResource(R.string.transactions_currency)
    // Each key rolls the amount to its new value.
    RollingNumber(
        value = digits.toLongOrNull() ?: 0,
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) { shown ->
        val text = buildAnnotatedString {
            withStyle(SpanStyle(color = if (shown == 0L) palette.muted else palette.ink)) { append(formatFcfa(shown)) }
            withStyle(SpanStyle(fontSize = CurrencySize, fontWeight = FontWeight.SemiBold, color = palette.muted)) {
                append(" ")
                append(currency)
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = AmountSize, fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = CauriTheme.palette.muted,
    )
}

// The accounts side by side, Cash in gold as in the mockup; more than four scroll sideways.
@Composable
private fun AccountChips(accounts: List<Account>, selected: Long?, onSelect: (Long) -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.xs + 2.dp),
    ) {
        accounts.forEach { account ->
            val cash = account.kind == AccountKind.CASH
            EntryChip(
                label = account.name,
                selected = account.id == selected,
                onClick = { onSelect(account.id) },
                modifier = Modifier.widthIn(min = AccountChipWidth),
                color = if (cash) palette.gold else palette.accent,
                soft = if (cash) palette.goldSoft else palette.accentSoft,
                text = if (cash) palette.goldText else palette.accentText,
            )
        }
    }
}

// The suggested categories and « Autre… », which opens every category of the type.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(state: QuickEntryUiState, actions: QuickEntryActions) {
    val shown: List<Category> = if (state.allCategoriesShown) state.categories else state.suggested
    if (state.categories.isEmpty()) return
    // Three lines at most: more categories scroll inside them.
    FlowRow(
        modifier = Modifier.heightIn(max = CategoriesMaxHeight).verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        shown.forEach { category ->
            EntryChip(
                label = category.name,
                selected = category.id == state.categoryId,
                onClick = { actions.onCategory(category.id) },
            )
        }
        if (!state.allCategoriesShown) {
            EntryChip(
                label = stringResource(R.string.transactions_other_category),
                selected = false,
                onClick = actions::onShowAllCategories,
            )
        }
    }
}
