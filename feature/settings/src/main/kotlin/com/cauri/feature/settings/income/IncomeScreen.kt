package com.cauri.feature.settings.income

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.NewFixedCost
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.Stepper
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.BackButton
import com.cauri.feature.settings.R
import com.cauri.feature.settings.RowGroup
import com.cauri.feature.settings.SectionLabel
import com.cauri.feature.settings.Separator
import com.cauri.feature.settings.SettingRow
import com.cauri.feature.settings.granted

private val RowHeight = 62.dp

/**
 * « Répartir mes revenus »: the share of each income put aside and the smallest income Cauri offers to split, then the
 * fixed costs, the expenses paid every month Cauri asks about first, and « + Ajouter une charge fixe ».
 */
@Composable
internal fun IncomeScreen(onBack: () -> Unit, viewModel: IncomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Named apart from the action it backs, which shares its name.
    val back = onBack
    val actions = remember(viewModel, back) {
        object : IncomeActions {
            override fun onBack() = back()

            override fun onSavingsRate(step: Int) = viewModel.onSavingsRate(step)

            override fun onThreshold(step: Int) = viewModel.onThreshold(step)

            override fun onConfirm(suggestion: FixedCostSuggestion) = viewModel.onConfirm(suggestion)

            override fun onIgnore(suggestion: FixedCostSuggestion) = viewModel.onIgnore(suggestion)

            override fun onEdit(cost: FixedCost?) = viewModel.onEdit(cost)

            override fun onDraftChange(draft: FixedCostDraft) = viewModel.onDraftChange(draft)

            override fun onDraftSave() = viewModel.onDraftSave()

            override fun onDraftDelete() = viewModel.onDraftDelete()

            override fun onEditClose() = viewModel.onEditClose()
        }
    }
    IncomeContent(state = state, actions = actions)
    state.draft?.let { FixedCostSheet(draft = it, actions = actions) }
}

@Composable
private fun IncomeContent(state: IncomeUiState, actions: IncomeActions) {
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l),
    ) {
        BackButton(onClick = actions::onBack)
        Text(
            text = stringResource(R.string.income_title),
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
            modifier = Modifier.padding(top = CauriSpacing.xl, bottom = CauriSpacing.xl),
        )
        SectionLabel(stringResource(R.string.income_each))
        SplitGroup(state = state, actions = actions)
        Hint(stringResource(R.string.income_each_hint))
        SectionLabel(stringResource(R.string.income_fixed), modifier = Modifier.padding(top = CauriSpacing.xxl))
        state.suggestions.forEach { suggestion ->
            SuggestionCard(suggestion = suggestion, actions = actions)
        }
        FixedCostsGroup(costs = state.fixedCosts, actions = actions)
        Hint(stringResource(R.string.income_fixed_hint))
    }
}

// The savings rate and the smallest income split, each with its stepper.
@Composable
private fun SplitGroup(state: IncomeUiState, actions: IncomeActions) {
    val settings = state.settings
    RowGroup {
        StepperRow(
            title = stringResource(R.string.income_savings),
            text = stringResource(R.string.income_savings_text),
        ) {
            Stepper(
                value = settings.savingsRate,
                canLower = settings.savingsRate > 0,
                canRaise = settings.savingsRate < IncomeViewModel.MAX_RATE,
                onStep = actions::onSavingsRate,
                label = stringResource(R.string.income_rate, settings.savingsRate),
            )
        }
        Separator()
        StepperRow(
            title = stringResource(R.string.income_threshold),
            text = stringResource(R.string.income_threshold_text),
        ) {
            Stepper(
                value = settings.threshold.toInt(),
                canLower = settings.threshold > 0,
                canRaise = settings.threshold < IncomeViewModel.MAX_THRESHOLD,
                onStep = actions::onThreshold,
                label = stringResource(R.string.income_amount, formatFcfa(settings.threshold)),
            )
        }
    }
}

// The fixed costs, those paid this month marked, then « + Ajouter une charge fixe ».
@Composable
private fun FixedCostsGroup(costs: List<FixedCost>, actions: IncomeActions) {
    RowGroup {
        costs.forEach { cost ->
            SettingRow(
                icon = DesignR.drawable.ic_clock,
                title = cost.name,
                text = stringResource(R.string.income_fixed_value, formatFcfa(cost.amount), cost.dayOfMonth),
                status = if (cost.paid) granted(R.string.income_fixed_paid) else null,
                onClick = { actions.onEdit(cost) },
            )
            Separator()
        }
        Text(
            text = stringResource(R.string.income_fixed_add),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = CauriTheme.palette.accentText,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { actions.onEdit(null) }
                .padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.l),
        )
    }
}

// « ENEO revient chaque mois », its usual amount and day, « Oui, c'est fixe » and « Non ».
@Composable
private fun SuggestionCard(suggestion: FixedCostSuggestion, actions: IncomeActions) {
    val palette = CauriTheme.palette
    val cost = suggestion.cost
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.group),
        modifier = Modifier.fillMaxWidth().padding(bottom = CauriSpacing.s),
    ) {
        Column(
            modifier = Modifier.padding(start = CauriSpacing.l, top = CauriSpacing.l, end = CauriSpacing.s),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.income_suggestion_title, cost.name),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(
                text = stringResource(
                    R.string.income_suggestion_text,
                    formatFcfa(cost.amount),
                    cost.dayOfMonth,
                    suggestion.months,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { actions.onIgnore(suggestion) }) {
                    Text(text = stringResource(R.string.income_suggestion_no), color = palette.muted)
                }
                TextButton(onClick = { actions.onConfirm(suggestion) }) {
                    Text(text = stringResource(R.string.income_suggestion_yes), color = palette.accentText)
                }
            }
        }
    }
}

// A setting: its title and text, then its stepper on the right.
@Composable
private fun StepperRow(title: String, text: String, stepper: @Composable () -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
        stepper()
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = CauriTheme.palette.muted,
        modifier = Modifier.padding(start = CauriSpacing.xs, top = CauriSpacing.s, end = CauriSpacing.xs),
    )
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IncomeContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            IncomeContent(
                state = IncomeUiState(
                    fixedCosts = listOf(
                        FixedCost(
                            id = 1,
                            name = "Loyer",
                            amount = 50_000,
                            dayOfMonth = 1,
                            categoryId = null,
                            counterparty = null,
                            paid = true,
                        ),
                    ),
                    suggestions = listOf(
                        FixedCostSuggestion(
                            cost = NewFixedCost(name = "ENEO", amount = 12_000, dayOfMonth = 7, counterparty = "ENEO"),
                            months = 3,
                        ),
                    ),
                ),
                actions = PreviewIncomeActions,
            )
        }
    }
}

/** Actions that do nothing, for previews. */
// One function per gesture of the screen.
@Suppress("TooManyFunctions")
private object PreviewIncomeActions : IncomeActions {
    override fun onBack() = Unit

    override fun onSavingsRate(step: Int) = Unit

    override fun onThreshold(step: Int) = Unit

    override fun onConfirm(suggestion: FixedCostSuggestion) = Unit

    override fun onIgnore(suggestion: FixedCostSuggestion) = Unit

    override fun onEdit(cost: FixedCost?) = Unit

    override fun onDraftChange(draft: FixedCostDraft) = Unit

    override fun onDraftSave() = Unit

    override fun onDraftDelete() = Unit

    override fun onEditClose() = Unit
}
