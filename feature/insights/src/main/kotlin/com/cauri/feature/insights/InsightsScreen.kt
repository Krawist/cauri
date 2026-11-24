package com.cauri.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.BudgetOverrun
import com.cauri.core.data.model.HabitCost
import com.cauri.core.data.model.Insights
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.format.formatFcfa
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// Above the floating bottom bar of the app.
private val BottomSpace = 120.dp

/**
 * « Analyses »: the month, its share of cash, the categories against their budgets, the budget gone over and the
 * costliest habit, then the budgets Cauri suggests from real spending, each accepted, adjusted or ignored.
 */
@Composable
internal fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = CauriTheme.palette
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(
            start = CauriSpacing.xl,
            top = CauriSpacing.xxl,
            end = CauriSpacing.xl,
            bottom = BottomSpace,
        ),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
    ) {
        item { Header(month = state.month) }
        state.insights?.let { insights -> figures(insights) }
        if (state.suggestions.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.insights_budgets_later),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.insights_budgets_suggested),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                )
            }
            items(items = state.suggestions, key = { it.categoryId }) { suggestion ->
                BudgetSuggestionCard(
                    suggestion = suggestion,
                    onAccept = { viewModel.onAccept(suggestion) },
                    onAdjust = { viewModel.onAdjust(suggestion) },
                    onIgnore = { viewModel.onIgnore(suggestion) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
    state.adjusting?.let { suggestion ->
        BudgetAmountDialog(
            suggestion = suggestion,
            onAmount = viewModel::onAmount,
            onDismiss = { viewModel.onAdjust(null) },
        )
    }
}

// The figures of the month, each card only once it has something to tell.
private fun LazyListScope.figures(insights: Insights) {
    if (insights.spent == 0L) {
        item {
            Text(
                text = stringResource(R.string.insights_nothing_spent),
                style = MaterialTheme.typography.bodyMedium,
                color = CauriTheme.palette.muted,
            )
        }
        return
    }
    item { CashShareCard(insights = insights) }
    if (insights.categories.isNotEmpty()) item { CategoriesCard(categories = insights.categories) }
    insights.overrun?.let { overrun -> item { OverrunCard(overrun = overrun) } }
    insights.habit?.let { habit -> item { HabitCard(habit = habit) } }
}

// « Transport déjà dépassé de 3 500 F », or that it will be, and where the month will end at this pace.
@Composable
private fun OverrunCard(overrun: BudgetOverrun) {
    val palette = CauriTheme.palette
    AdviceCard(
        icon = R.drawable.ic_trend,
        tint = palette.coralText,
        fill = palette.coralSoft,
        title = if (overrun.over > 0) {
            stringResource(R.string.insights_overrun, overrun.name, formatFcfa(overrun.over))
        } else {
            stringResource(R.string.insights_overrun_coming, overrun.name)
        },
        text = stringResource(R.string.insights_overrun_projection, formatFcfa(overrun.projected)),
    )
}

// « Beignets : ≈ 122 000 F par an ».
@Composable
private fun HabitCard(habit: HabitCost) {
    val palette = CauriTheme.palette
    AdviceCard(
        icon = DesignR.drawable.ic_clock,
        tint = palette.goldText,
        fill = palette.goldSoft,
        title = stringResource(R.string.insights_habit_yearly, habit.name, formatFcfa(habit.yearly)),
        text = pluralStringResource(R.plurals.insights_habit_uses, habit.uses, habit.uses),
    )
}

// « Analyses » and the month.
@Composable
private fun Header(month: YearMonth) {
    val palette = CauriTheme.palette
    val locale = LocalConfiguration.current.locales[0]
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.insights_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.ink,
            modifier = Modifier.weight(1f),
        )
        GlassSurface(shape = CircleShape) {
            Text(
                text = month.format(DateTimeFormatter.ofPattern("LLLL", locale)).replaceFirstChar {
                    it.titlecase(locale)
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = palette.ink,
                modifier = Modifier.padding(horizontal = CauriSpacing.m + 2.dp, vertical = CauriSpacing.s + 2.dp),
            )
        }
    }
}
