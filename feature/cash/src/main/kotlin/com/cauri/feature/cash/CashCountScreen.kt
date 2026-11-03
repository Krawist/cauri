package com.cauri.feature.cash

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 56.dp
private val TotalFieldHeight = 64.dp

/**
 * « Compter mon cash »: banknotes and coins, or the total typed, against the estimated cash. [onBack] closes it, also
 * once the count is saved; [onSplitGap] receives the counted and expected amounts when money is missing.
 */
@Composable
internal fun CashCountScreen(
    onBack: () -> Unit,
    onSplitGap: (counted: Long, expected: Long) -> Unit,
    viewModel: CashCountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }
    LaunchedEffect(state.splitting) {
        val expected = state.expected
        if (state.splitting && expected != null) {
            onSplitGap(state.total, expected)
            viewModel.onSplittingHandedOn()
        }
    }
    CashCountContent(state = state, actions = viewModel, onBack = onBack)
}

@Composable
private fun CashCountContent(state: CashCountUiState, actions: CashCountActions, onBack: () -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = CauriSpacing.xl)) {
        Header(onBack = onBack, onClearAll = actions::onClearAll)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            if (state.typingTotal) {
                TotalField(total = state.typedTotal, onChange = actions::onTypedTotalChange)
            } else {
                Denominations(R.string.cash_count_notes, Denomination.entries.filterNot { it.coin }, state, actions)
                Denominations(R.string.cash_count_coins, Denomination.entries.filter { it.coin }, state, actions)
            }
            TextButton(onClick = { actions.onTypeTotal(!state.typingTotal) }) {
                Text(
                    text = stringResource(
                        if (state.typingTotal) R.string.cash_count_by_denomination else R.string.cash_count_type_total,
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.accentText,
                )
            }
        }
        CountSummary(total = state.total, expected = state.expected, gap = state.gap)
        val missing = (state.gap ?: 0) > 0
        Button(
            onClick = {
                haptics.confirm()
                actions.onConfirm()
            },
            modifier = Modifier.fillMaxWidth().padding(
                top = CauriSpacing.m,
                bottom = CauriSpacing.l,
            ).height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(
                text = stringResource(if (missing) R.string.cash_count_split else R.string.cash_count_confirm),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit, onClearAll: () -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.l, bottom = CauriSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CashBackButton(onClick = onBack)
        Text(
            text = stringResource(R.string.cash_count_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = palette.ink,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onClearAll) {
            Text(
                text = stringResource(R.string.cash_count_clear),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.coralText,
            )
        }
    }
}

// Two tiles a row, as in the mockup.
@Composable
private fun Denominations(
    title: Int,
    denominations: List<Denomination>,
    state: CashCountUiState,
    actions: CashCountActions,
) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = CauriTheme.palette.muted,
        modifier = Modifier.padding(top = CauriSpacing.s),
    )
    denominations.chunked(2).forEach { pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
            pair.forEach { denomination ->
                DenominationTile(
                    denomination = denomination,
                    count = state.counts[denomination] ?: 0,
                    actions = actions,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TotalField(total: String, onChange: (String) -> Unit) {
    Text(
        text = stringResource(R.string.cash_count_total_question),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = CauriTheme.palette.muted,
        modifier = Modifier.padding(top = CauriSpacing.s),
    )
    CauriTextField(
        value = total,
        onValueChange = onChange,
        missing = false,
        textStyle = MaterialTheme.typography.headlineMedium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        height = TotalFieldHeight,
        visualTransformation = FcfaVisualTransformation,
        autoFocus = true,
        trailing = {
            Text(
                text = stringResource(R.string.cash_currency),
                style = MaterialTheme.typography.titleMedium,
                color = CauriTheme.palette.muted,
            )
        },
    )
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CashCountContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            CashCountContent(
                state = CashCountUiState(
                    counts = mapOf(Denomination.NOTE_2000 to 1, Denomination.COIN_500 to 1, Denomination.COIN_100 to 2),
                    total = 2_700,
                    expected = 7_500,
                    gap = 4_800,
                ),
                actions = PreviewCashCountActions,
                onBack = {},
            )
        }
    }
}

/** Actions that do nothing, for previews. */
private object PreviewCashCountActions : CashCountActions {
    override fun onAdd(denomination: Denomination) = Unit

    override fun onRemove(denomination: Denomination) = Unit

    override fun onClearAll() = Unit

    override fun onTypeTotal(typing: Boolean) = Unit

    override fun onTypedTotalChange(total: String) = Unit

    override fun onConfirm() = Unit
}
