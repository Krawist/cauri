package com.cauri.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.cauri.core.data.model.AccountBalance
import com.cauri.core.design.component.Keypad
import com.cauri.core.design.component.RollingNumber
import com.cauri.core.design.component.backdrop
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.component.screenBackdropShapes
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassScreen
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 56.dp
private val AmountSize = 56.sp
private val CurrencySize = 28.sp

// Up to 99 999 999 F, as the cash count.
private const val MAX_AMOUNT_DIGITS = 8

/**
 * « Solde d'Orange Money »: the balance the user reads in the operator's app, against the one Cauri shows. Less than
 * shown, « Répartir l'écart » opens the split of the gap through [onSplitGap] (typed, shown); « Enregistrer » keeps the
 * balance and leaves the gap unsplit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BalanceSheet(
    balance: AccountBalance,
    onSplitGap: (typed: Long, shown: Long) -> Unit,
    onDismiss: () -> Unit,
    viewModel: BalanceViewModel = hiltViewModel(),
) {
    val palette = CauriTheme.palette
    var digits by rememberSaveable { mutableStateOf("") }
    val typed = digits.toLongOrNull()
    val shown = balance.amount
    // The first balance has nothing to compare to.
    val gap = if (typed != null && shown != null) shown - typed else 0
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.background,
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        // A glass screen of its own over the screens' shapes, as the quick entry: its keys frost them.
        GlassScreen(
            fillMaxSize = false,
            background = { Box(Modifier.fillMaxSize().backdrop(screenBackdropShapes())) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // A small screen scrolls rather than cutting « Enregistrer ».
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(CauriSpacing.l),
            ) {
                Text(
                    text = stringResource(R.string.home_balance_title, balance.account.name),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = stringResource(R.string.home_balance_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
                TypedAmount(typed = typed)
                if (shown != null) Comparison(shown = shown, typed = typed, gap = gap)
                // Cauri's own keypad, as the quick entry: never the phone's keyboard.
                Keypad(
                    onDigits = { key -> digits = (digits + key).trimStart('0').take(MAX_AMOUNT_DIGITS) },
                    onDelete = { digits = digits.dropLast(1) },
                    onClear = { digits = "" },
                )
                Answers(
                    typed = typed,
                    splits = gap > 0,
                    onSplit = { if (typed != null && shown != null) onSplitGap(typed, shown) },
                    onSave = {
                        typed?.let { viewModel.onSave(accountId = balance.account.id, amount = it) }
                        onDismiss()
                    },
                )
            }
        }
    }
}

// « 10 000 F », large and centred, the currency smaller; « 0 F » before the first digit.
@Composable
private fun TypedAmount(typed: Long?) {
    val palette = CauriTheme.palette
    val currency = stringResource(R.string.home_currency)
    // Each key rolls the amount to its new value.
    RollingNumber(
        value = typed ?: 0,
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
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// « Affiché : 5 000 F » and, once typed, « il manque 1 000 F », « 500 F de plus » or « Juste ».
@Composable
private fun Comparison(shown: Long, typed: Long?, gap: Long) {
    val palette = CauriTheme.palette
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(R.string.home_balance_shown, formatFcfa(shown)),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
        )
        if (typed != null) {
            Text(
                text = when {
                    gap > 0 -> stringResource(R.string.home_balance_missing, formatFcfa(gap))
                    gap < 0 -> stringResource(R.string.home_balance_more, formatFcfa(-gap))
                    else -> stringResource(R.string.home_balance_even)
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (gap > 0) palette.coralText else palette.ink,
            )
        }
    }
}

// « Répartir l'écart » first when money is missing, « Enregistrer » always: it keeps the balance, the gap unsplit.
@Composable
private fun Answers(typed: Long?, splits: Boolean, onSplit: () -> Unit, onSave: () -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    if (splits) {
        Button(
            onClick = {
                haptics.select()
                onSplit()
            },
            modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.s).height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) { Text(text = stringResource(R.string.home_balance_split), style = MaterialTheme.typography.titleMedium) }
    }
    Button(
        onClick = {
            haptics.confirm()
            onSave()
        },
        enabled = typed != null,
        modifier = Modifier.fillMaxWidth().padding(top = if (splits) 0.dp else CauriSpacing.s).height(ButtonHeight),
        shape = CircleShape,
        colors = if (splits) {
            ButtonDefaults.buttonColors(containerColor = palette.chip, contentColor = palette.ink)
        } else {
            ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent)
        },
    ) { Text(text = stringResource(R.string.home_balance_save), style = MaterialTheme.typography.titleMedium) }
}
