package com.cauri.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Category
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 56.dp
private val ChipHeight = 40.dp
private val SelectedBorderWidth = 1.5.dp

/**
 * Sheet to add or edit a habit: its name, amount and category. Editing, it also deletes the habit and leads to the
 * list to reorder them, once there are several.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitSheet(draft: HabitDraft, categories: List<Category>, canReorder: Boolean, actions: HabitActions) {
    val palette = CauriTheme.palette
    ModalBottomSheet(
        onDismissRequest = actions::onEditClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        // The sheet is its own window: there is nothing of the screen to blur, so the sheet colour is opaque.
        containerColor = palette.sheet.compositeOver(palette.background),
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.l),
        ) {
            Text(
                text = stringResource(if (draft.id == null) R.string.home_habit_new else R.string.home_habit_edit),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = CauriSpacing.l),
            )
            DraftFields(draft = draft, onChange = actions::onDraftChange)
            Label(R.string.home_habit_category, modifier = Modifier.padding(top = CauriSpacing.l))
            CategoryChips(
                categories = categories,
                selected = draft.categoryId,
                onSelect = { id -> actions.onDraftChange(draft.copy(categoryId = id)) },
            )
            Button(
                onClick = actions::onDraftSave,
                modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.xl).height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.home_habit_save), style = MaterialTheme.typography.titleMedium)
            }
            if (draft.id != null) EditActions(canReorder = canReorder, actions = actions)
        }
    }
}

@Composable
private fun DraftFields(draft: HabitDraft, onChange: (HabitDraft) -> Unit) {
    val palette = CauriTheme.palette
    Label(R.string.home_habit_name)
    CauriTextField(
        value = draft.name,
        onValueChange = { onChange(draft.copy(name = it, nameMissing = false)) },
        missing = draft.nameMissing,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next,
        ),
        placeholder = stringResource(R.string.home_habit_name_hint),
    )
    Label(R.string.home_habit_amount, modifier = Modifier.padding(top = CauriSpacing.l))
    CauriTextField(
        value = draft.amount,
        onValueChange = { onChange(draft.copy(amount = it, amountMissing = false)) },
        missing = draft.amountMissing,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        visualTransformation = FcfaVisualTransformation,
        placeholder = stringResource(R.string.home_habit_amount_hint),
        trailing = {
            Text(
                text = stringResource(R.string.home_currency),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.muted,
            )
        },
    )
}

// A tap chooses a category, a second tap on it leaves the habit without category.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(categories: List<Category>, selected: Long?, onSelect: (Long?) -> Unit) {
    val palette = CauriTheme.palette
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        categories.forEach { category ->
            val chosen = category.id == selected
            GlassSurface(
                shape = CircleShape,
                modifier = Modifier
                    .height(ChipHeight)
                    .then(if (chosen) Modifier.border(SelectedBorderWidth, palette.accent, CircleShape) else Modifier),
                interaction = Modifier.selectable(
                    selected = chosen,
                    role = Role.RadioButton,
                    onClick = { onSelect(if (chosen) null else category.id) },
                ),
            ) {
                if (chosen) Box(Modifier.matchParentSize().background(palette.accentSoft))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (chosen) palette.accentText else palette.ink,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = CauriSpacing.l),
                )
            }
        }
    }
}

@Composable
private fun EditActions(canReorder: Boolean, actions: HabitActions) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.s),
        horizontalArrangement = if (canReorder) Arrangement.SpaceBetween else Arrangement.Center,
    ) {
        if (canReorder) {
            TextButton(
                onClick = { actions.onManageHabits(open = true) },
                colors = ButtonDefaults.textButtonColors(contentColor = palette.accentText),
            ) {
                Text(text = stringResource(R.string.home_habits_reorder), style = MaterialTheme.typography.bodyLarge)
            }
        }
        TextButton(
            onClick = actions::onDraftDelete,
            colors = ButtonDefaults.textButtonColors(contentColor = palette.coralText),
        ) {
            Text(text = stringResource(R.string.home_habit_delete), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun Label(@StringRes text: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelMedium,
        color = CauriTheme.palette.muted,
        modifier = modifier.padding(bottom = CauriSpacing.s),
    )
}
