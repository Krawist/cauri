package com.cauri.feature.transactions.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R
import com.cauri.feature.transactions.entry.EntryChip
import com.cauri.feature.transactions.entry.EntryChipHeight

// Three lines of chips and the gaps between them, as the quick entry.
private val CategoriesMaxHeight = EntryChipHeight * 3 + CauriSpacing.s * 2

/**
 * « Catégorie », with « Proposée par Cauri » while the proposal is the one chosen: the suggested categories, then
 * « Toutes… » for every category of the type.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditCategories(state: EditUiState, actions: EditActions) {
    if (state.categories.isEmpty()) return
    val palette = CauriTheme.palette
    val chosen = state.categories.firstOrNull { it.id == state.categoryId }
    // The category chosen before stays in sight, even when Cauri no longer suggests it.
    val shown = if (state.allCategoriesShown) state.categories else (state.suggested + listOfNotNull(chosen)).distinct()
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Label(text = stringResource(R.string.transactions_edit_category))
            if (chosen != null && chosen.id == state.suggested.firstOrNull()?.id) {
                Text(
                    text = stringResource(R.string.transactions_edit_proposed),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = palette.accentText,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                )
            }
        }
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
            if (!state.allCategoriesShown && state.categories.size > shown.size) {
                EntryChip(
                    label = stringResource(R.string.transactions_edit_all_categories),
                    selected = false,
                    onClick = actions::onShowAllCategories,
                )
            }
        }
    }
}
