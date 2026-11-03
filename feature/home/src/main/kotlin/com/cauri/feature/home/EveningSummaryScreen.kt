package com.cauri.feature.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ButtonHeight = 56.dp
private val LaterHeight = 44.dp

/**
 * « Ce soir », opened by the evening summary: what was spent today and of it in cash, then every card still to
 * confirm, one line each. ✓ confirms a line, a swipe to the left sets it aside, a tap on the amount types another, a
 * tap on the line opens the edit sheet through [onEdit]; « Tout valider » confirms every line with an amount,
 * « Plus tard » leaves them for tomorrow.
 */
@Composable
internal fun EveningSummaryScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: EveningSummaryViewModel = hiltViewModel(),
    cards: ConfirmationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val confirmation by cards.uiState.collectAsStateWithLifecycle()
    val ready = confirmation.cards.count { it.amount > 0 }
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l)
                // Room for the buttons at the bottom.
                .padding(bottom = ButtonHeight + LaterHeight + CauriSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
        ) {
            Header(day = state.day, count = confirmation.cards.size, onBack = onBack)
            Totals(state = state)
            if (confirmation.cards.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_summary_all_done),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CauriTheme.palette.muted,
                    modifier = Modifier.padding(top = CauriSpacing.l),
                )
            } else {
                Lines(cards = confirmation.cards, viewModel = cards, onEdit = onEdit)
            }
        }
        Answers(
            ready = ready,
            empty = confirmation.cards.isEmpty(),
            cards = cards,
            onBack = onBack,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
    confirmation.cards.firstOrNull { it.operation.transactionId == confirmation.editingAmount }?.let { card ->
        AmountDialog(
            amount = card.amount.takeIf { it > 0 }?.let { it + card.operation.fees } ?: 0,
            onAmount = cards::onAmount,
            onDismiss = { cards.onEditAmount(null) },
        )
    }
}

// The undo bar, « Tout valider » the [ready] lines with an amount, then « Plus tard », or « Fermer » once [empty].
@Composable
private fun Answers(
    ready: Int,
    empty: Boolean,
    cards: ConfirmationViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCauriHaptics()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        ConfirmationUndoBar(viewModel = cards)
        if (ready > 0) {
            Button(
                onClick = {
                    haptics.confirm()
                    cards.onConfirmAll()
                },
                modifier = Modifier.fillMaxWidth().height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CauriTheme.palette.accent,
                    contentColor = CauriTheme.palette.onAccent,
                ),
            ) {
                Text(
                    text = stringResource(R.string.home_summary_confirm_all, ready),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(LaterHeight)) {
            Text(
                text = stringResource(if (empty) R.string.home_summary_close else R.string.home_summary_later),
                style = MaterialTheme.typography.titleSmall,
                color = CauriTheme.palette.muted,
            )
        }
    }
}

// The back button, « Ce soir », then the day and how many lines wait.
@Composable
private fun Header(day: LocalDate, count: Int, onBack: () -> Unit) {
    val palette = CauriTheme.palette
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m), verticalAlignment = Alignment.CenterVertically) {
        val description = stringResource(R.string.home_summary_back)
        GlassSurface(
            shape = CircleShape,
            modifier = Modifier.size(CauriSpacing.touchTarget),
            interaction = Modifier.clickable(onClickLabel = description, role = Role.Button, onClick = onBack),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_back),
                contentDescription = description,
                tint = palette.ink,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = stringResource(R.string.home_summary_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
            Text(
                text = pluralStringResource(R.plurals.home_summary_subtitle, count, dayName(day), count),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
    }
}

// « Dépensé aujourd'hui » and « Dont cash », in gold.
@Composable
private fun Totals(state: EveningSummaryUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Total(
            label = stringResource(R.string.home_summary_spent),
            amount = state.spent,
            cash = false,
            modifier = Modifier.weight(1f),
        )
        Total(
            label = stringResource(R.string.home_summary_cash),
            amount = state.cash,
            cash = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Total(label: String, amount: Long, cash: Boolean, modifier: Modifier = Modifier) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.tile), modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (cash) palette.goldText else palette.muted,
            )
            Text(
                text = stringResource(R.string.home_amount, formatFcfa(amount)),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
        }
    }
}

// « À confirmer », the lines in one glass card, then how to answer them.
@Composable
private fun Lines(cards: List<ConfirmationCard>, viewModel: ConfirmationViewModel, onEdit: (Long) -> Unit) {
    val palette = CauriTheme.palette
    Text(
        text = stringResource(R.string.home_pending_title),
        style = MaterialTheme.typography.labelMedium,
        color = palette.muted,
        modifier = Modifier.padding(start = CauriSpacing.xs, top = CauriSpacing.xs),
    )
    GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
        Column {
            cards.forEachIndexed { index, card ->
                if (index > 0) HorizontalDivider(color = palette.chip)
                SummaryLine(card = card, viewModel = viewModel, onEdit = onEdit)
            }
        }
    }
    Text(
        text = stringResource(R.string.home_summary_hint),
        style = MaterialTheme.typography.bodySmall,
        color = palette.muted,
        modifier = Modifier.padding(horizontal = CauriSpacing.xs),
    )
}

// « Mardi 3 novembre », in the user's language.
@Composable
private fun dayName(day: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    return day.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale)).replaceFirstChar { it.titlecase(locale) }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TotalsPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background).padding(CauriSpacing.xl)) {
            Totals(EveningSummaryUiState(day = LocalDate.now(), spent = 6_800, cash = 4_300))
        }
    }
}
