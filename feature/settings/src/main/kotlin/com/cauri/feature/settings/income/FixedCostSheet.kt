package com.cauri.feature.settings.income

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.Stepper
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.R

private val ButtonHeight = 56.dp

/**
 * The sheet of a fixed cost: its name, its amount a month and its day, « Enregistrer », and « Supprimer » once
 * saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FixedCostSheet(draft: FixedCostDraft, actions: IncomeActions) {
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
                text = stringResource(if (draft.id == null) R.string.income_fixed_new else R.string.income_fixed_edit),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = CauriSpacing.l),
            )
            Fields(draft = draft, onChange = actions::onDraftChange)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.l),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Label(R.string.income_fixed_day, modifier = Modifier.weight(1f))
                Stepper(
                    value = draft.dayOfMonth,
                    canLower = draft.dayOfMonth > 1,
                    canRaise = draft.dayOfMonth < LAST_DAY,
                    onStep = { actions.onDraftChange(draft.copy(dayOfMonth = draft.dayOfMonth + it)) },
                )
            }
            Button(
                onClick = actions::onDraftSave,
                modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.xl).height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.income_fixed_save), style = MaterialTheme.typography.titleMedium)
            }
            if (draft.id != null) {
                TextButton(
                    onClick = actions::onDraftDelete,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = CauriSpacing.s),
                    colors = ButtonDefaults.textButtonColors(contentColor = palette.coralText),
                ) {
                    Text(
                        text = stringResource(R.string.income_fixed_delete),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun Fields(draft: FixedCostDraft, onChange: (FixedCostDraft) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Label(R.string.income_fixed_name)
        CauriTextField(
            value = draft.name,
            onValueChange = { onChange(draft.copy(name = it, nameMissing = false)) },
            missing = draft.nameMissing,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            placeholder = stringResource(R.string.income_fixed_name_hint),
        )
        Label(R.string.income_fixed_amount, modifier = Modifier.padding(top = CauriSpacing.s))
        CauriTextField(
            value = draft.amount,
            onValueChange = { onChange(draft.copy(amount = it, amountMissing = false)) },
            missing = draft.amountMissing,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            visualTransformation = FcfaVisualTransformation,
            placeholder = stringResource(R.string.income_fixed_amount_hint),
            trailing = {
                Text(
                    text = stringResource(R.string.income_currency),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CauriTheme.palette.muted,
                )
            },
        )
    }
}

@Composable
private fun Label(@StringRes text: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelMedium,
        color = CauriTheme.palette.muted,
        modifier = modifier,
    )
}

private const val LAST_DAY = 31
