package com.cauri.feature.cash

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.Category
import com.cauri.core.design.component.dashedBorder
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.format.formatMoment
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.time.Instant

private val ButtonHeight = 56.dp
private val AddButtonHeight = 48.dp
private val ProgressHeight = 8.dp

/**
 * « Où sont partis 4 500 F ? »: the moments Cauri noticed and the expenses added, each with an amount, the bar of
 * what is split, and the rest recorded as « Divers cash ». [onBack] returns to the count; [onSaved] closes the cash
 * screens once everything is saved.
 */
@Composable
internal fun GapSplitScreen(onBack: () -> Unit, onSaved: () -> Unit, viewModel: GapSplitViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }
    GapSplitContent(state = state, actions = viewModel, onBack = onBack)
    if (state.picking) CategoryPickerSheet(categories = state.categories, actions = viewModel)
}

@Composable
private fun GapSplitContent(state: GapSplitUiState, actions: GapSplitActions, onBack: () -> Unit) {
    val palette = CauriTheme.palette
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = CauriSpacing.xl)) {
        Header(gap = state.gap, since = state.since, accountName = state.accountName, onBack = onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
        ) {
            SplitProgress(assigned = state.assigned, gap = state.gap)
            // « Cauri a repéré ces moments » only above the moments Cauri suggested.
            if (state.lines.any { it.fromHabit }) {
                Text(
                    text = stringResource(R.string.cash_split_noticed),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.muted,
                    modifier = Modifier.padding(top = CauriSpacing.xs),
                )
            }
            if (state.lines.isNotEmpty()) Lines(state = state, actions = actions)
            AddExpenseButton(onClick = { actions.onPickCategory(open = true) })
        }
        Rest(rest = state.rest, digital = state.accountName != null)
        Button(
            onClick = actions::onSave,
            modifier = Modifier.fillMaxWidth().padding(
                top = CauriSpacing.m,
                bottom = CauriSpacing.l,
            ).height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(text = stringResource(R.string.cash_split_save), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Header(gap: Long, since: Instant?, accountName: String?, onBack: () -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.l, bottom = CauriSpacing.l),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CashBackButton(onClick = onBack)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.cash_split_title_amount, formatFcfa(gap)),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
            if (since != null) {
                Text(
                    text =
                    accountName?.let { stringResource(R.string.cash_split_since_balance, it, formatMoment(since)) }
                        ?: stringResource(R.string.cash_split_since, formatMoment(since)),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }
    }
}

@Composable
private fun SplitProgress(assigned: Long, gap: Long) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.tile), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.cash_split_assigned, formatFcfa(assigned)),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.cash_split_of, formatFcfa(gap)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            }
            LinearProgressIndicator(
                progress = { if (gap > 0) (assigned.toFloat() / gap).coerceAtMost(1f) else 1f },
                modifier = Modifier.fillMaxWidth().height(ProgressHeight).clip(CircleShape),
                color = palette.accent,
                trackColor = palette.track,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun Lines(state: GapSplitUiState, actions: GapSplitActions) {
    GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = CauriSpacing.l)) {
            state.lines.forEachIndexed { index, line ->
                if (index > 0) HorizontalDivider(color = CauriTheme.palette.chip)
                GapLineRow(line = line, onAmountChange = { actions.onAmountChange(line.key, it) })
            }
        }
    }
}

@Composable
private fun AddExpenseButton(onClick: () -> Unit) {
    val palette = CauriTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AddButtonHeight)
            .clip(RoundedCornerShape(CauriRadius.field + 4.dp))
            .dashedBorder(palette.handle, radius = CauriRadius.field + 4.dp)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.cash_split_add),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = palette.ink,
        )
    }
}

// « Reste non identifié », recorded as « Divers cash » for the user to file later; nothing for a digital account.
@Composable
private fun Rest(rest: Long, digital: Boolean) {
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier.padding(horizontal = CauriSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.cash_split_rest),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.cash_amount, formatFcfa(rest)),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
        }
        Text(
            text = stringResource(
                if (digital) R.string.cash_split_rest_hint_balance else R.string.cash_split_rest_hint,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GapSplitContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            GapSplitContent(
                state = GapSplitUiState(
                    gap = 4_500,
                    since = Instant.now(),
                    lines = listOf(
                        GapLine(
                            key = "habit-1",
                            name = "Moto",
                            fromHabit = true,
                            hint = 500,
                            categoryId = null,
                            note = "Moto",
                            amount = "2000",
                        ),
                        GapLine(
                            key = "habit-2",
                            name = "Beignets",
                            fromHabit = true,
                            hint = 300,
                            categoryId = null,
                            note = "Beignets",
                        ),
                    ),
                    assigned = 2_000,
                    rest = 2_500,
                ),
                actions = PreviewGapSplitActions,
                onBack = {},
            )
        }
    }
}

/** Actions that do nothing, for previews. */
private object PreviewGapSplitActions : GapSplitActions {
    override fun onAmountChange(key: String, amount: String) = Unit

    override fun onPickCategory(open: Boolean) = Unit

    override fun onAddExpense(category: Category) = Unit

    override fun onSave() = Unit
}
