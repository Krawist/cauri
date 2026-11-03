package com.cauri.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

// Above the floating bottom bar of the app.
private val BottomSpace = 120.dp

/** « Analyses »: the budgets Cauri suggests from real spending, each accepted, adjusted or ignored. */
@Composable
internal fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = CauriTheme.palette
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
        item {
            Text(
                text = stringResource(R.string.insights_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
        }
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
