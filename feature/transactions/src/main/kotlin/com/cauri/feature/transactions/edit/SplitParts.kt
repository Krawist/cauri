package com.cauri.feature.transactions.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Category
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.dashedBorder
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R
import com.cauri.feature.transactions.entry.EntryChip

private val ButtonHeight = 48.dp
private val PartHeight = 48.dp
private val PartAmountWidth = 132.dp
private val IconSize = 18.dp

/**
 * « Répartir sur plusieurs catégories », dashed as in the mockup, then the parts: a category and an amount each,
 * « Ajouter une part », and what is left to the category chosen above.
 */
@Composable
internal fun SplitParts(state: EditUiState, actions: EditActions) {
    if (state.categories.isEmpty()) return
    val palette = CauriTheme.palette
    if (state.parts.isEmpty()) {
        DashedButton(
            text = stringResource(R.string.transactions_edit_split),
            icon = R.drawable.ic_split,
            onClick = actions::onAddPart,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        state.parts.forEachIndexed { index, part ->
            PartRow(index = index, part = part, categories = state.categories, actions = actions)
        }
        DashedButton(
            text = stringResource(R.string.transactions_edit_part_add),
            icon = DesignR.drawable.ic_nav_add,
            onClick = actions::onAddPart,
        )
        val rest = state.categories.firstOrNull { it.id == state.categoryId }
        Text(
            text = when {
                state.remainder <= 0 -> stringResource(R.string.transactions_edit_parts_too_high)
                rest != null -> stringResource(R.string.transactions_edit_rest, rest.name, formatFcfa(state.remainder))
                else -> stringResource(R.string.transactions_edit_rest_none, formatFcfa(state.remainder))
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.remainder <= 0) palette.coralText else palette.muted,
        )
    }
}

// A category to choose from a menu, the amount of the part, and a cross that removes it.
@Composable
private fun PartRow(index: Int, part: PartDraft, categories: List<Category>, actions: EditActions) {
    val palette = CauriTheme.palette
    var open by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            EntryChip(
                label = categories.firstOrNull { it.id == part.categoryId }?.name
                    ?: stringResource(R.string.transactions_edit_choose),
                selected = part.categoryId != null,
                onClick = { open = true },
                modifier = Modifier.fillMaxWidth().height(PartHeight),
            )
            DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = palette.background) {
                categories.forEach {
                    DropdownMenuItem(
                        text = { Text(text = it.name) },
                        onClick = {
                            actions.onPartCategory(index = index, categoryId = it.id)
                            open = false
                        },
                    )
                }
            }
        }
        Box(modifier = Modifier.width(PartAmountWidth)) {
            CauriTextField(
                value = part.amount,
                onValueChange = { actions.onPartAmount(index = index, digits = it) },
                missing = false,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                height = PartHeight,
                visualTransformation = FcfaVisualTransformation,
                trailing = { Text(text = stringResource(R.string.transactions_currency), color = palette.muted) },
            )
        }
        IconButton(onClick = { actions.onRemovePart(index) }) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.transactions_edit_part_remove),
                tint = palette.muted,
            )
        }
    }
}

@Composable
private fun DashedButton(text: String, icon: Int, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .clip(RoundedCornerShape(CauriRadius.icon))
            .dashedBorder(palette.handle, radius = CauriRadius.icon)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CauriSpacing.l),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m - 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = palette.ink,
            modifier = Modifier.size(IconSize),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = palette.ink,
        )
    }
}
