package com.cauri.feature.settings.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.cauri.core.data.model.Category
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.R

/**
 * The categories of the same kind to merge the draft into (never itself nor its own sub-categories), then a last
 * question, the merge being final.
 */
@Composable
internal fun MergeDialog(draft: CategoryDraft, state: CategoriesUiState, actions: CategoriesActions) {
    val palette = CauriTheme.palette
    var chosen by remember { mutableStateOf<Category?>(null) }
    val targets = state.categories.filter {
        it.nature == draft.nature && it.id != draft.id && it.parentId != draft.id
    }
    val target = chosen
    AlertDialog(
        onDismissRequest = { actions.onAskMerge(false) },
        containerColor = palette.background,
        title = {
            Text(
                if (target == null) {
                    stringResource(R.string.category_list_merge_title, draft.name)
                } else {
                    stringResource(R.string.category_list_merge_confirm_title, draft.name, target.name)
                },
            )
        },
        text = {
            if (target == null) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    targets.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) { chosen = category }
                                .padding(vertical = CauriSpacing.m),
                            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryBadge(category)
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.ink,
                            )
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.category_list_merge_confirm_text, draft.name, target.name))
            }
        },
        confirmButton = {
            if (target != null) {
                TextButton(onClick = { actions.onMerge(target.id) }) {
                    Text(stringResource(R.string.category_list_merge_confirm), color = palette.coralText)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { actions.onAskMerge(false) }) {
                Text(stringResource(R.string.category_list_cancel), color = palette.muted)
            }
        },
    )
}

/** The archived categories; a tap brings one back with its sub-categories. */
@Composable
internal fun ArchivedDialog(archived: List<Category>, actions: CategoriesActions) {
    val palette = CauriTheme.palette
    AlertDialog(
        onDismissRequest = { actions.onShowArchived(false) },
        containerColor = palette.background,
        title = { Text(stringResource(R.string.category_list_archived_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.category_list_archived_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
                archived.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { actions.onRestore(category.id) }
                            .padding(vertical = CauriSpacing.m),
                        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryBadge(category)
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.ink,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(R.string.category_list_restore),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.accentText,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { actions.onShowArchived(false) }) {
                Text(stringResource(R.string.category_list_close_list), color = palette.accentText)
            }
        },
    )
}
