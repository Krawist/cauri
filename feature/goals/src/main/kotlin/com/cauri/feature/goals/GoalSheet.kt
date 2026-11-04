package com.cauri.feature.goals

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.GoalKind
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.Stepper
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ButtonHeight = 56.dp
private const val LAST_DAY = 31

// The date row a dialog is open for.
private enum class DateField { DEADLINE, PAYOUT }

/**
 * The sheet of a goal: its kind for a new one, its name and amount, then a saving's deadline and the money put aside
 * or taken back by hand, a tontine's day of contribution and payout, or an envelope's category; « Mettre en premier »,
 * « Enregistrer » and « Supprimer ».
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalSheet(draft: GoalDraft, categories: List<Category>, actions: GoalActions) {
    val palette = CauriTheme.palette
    var choosing by rememberSaveable { mutableStateOf<DateField?>(null) }
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
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.l),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            Text(
                text = stringResource(if (draft.id == null) R.string.goals_new else R.string.goals_edit),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = CauriSpacing.s),
            )
            if (draft.id == null) KindChips(draft = draft, onChange = actions::onDraftChange)
            Fields(draft = draft, onChange = actions::onDraftChange)
            when (draft.kind) {
                GoalKind.SAVING -> {
                    DateRow(R.string.goals_deadline, draft.deadline) { choosing = DateField.DEADLINE }
                    if (draft.id != null) Moving(draft = draft, actions = actions)
                }
                GoalKind.TONTINE -> {
                    DayRow(draft = draft, onChange = actions::onDraftChange)
                    DateRow(R.string.goals_payout, draft.payoutDate) { choosing = DateField.PAYOUT }
                }
                GoalKind.ENVELOPE -> CategoryChips(
                    draft = draft,
                    categories = categories,
                    onChange = actions::onDraftChange,
                )
            }
            Answers(draft = draft, actions = actions)
        }
    }
    choosing?.let { field ->
        DateDialog(
            day = if (field == DateField.DEADLINE) draft.deadline else draft.payoutDate,
            today = LocalDate.now(),
            onDate = { date ->
                choosing = null
                actions.onDraftChange(
                    if (field == DateField.DEADLINE) draft.copy(deadline = date) else draft.copy(payoutDate = date),
                )
            },
            onDismiss = { choosing = null },
        )
    }
}

@Composable
private fun Fields(draft: GoalDraft, onChange: (GoalDraft) -> Unit) {
    Label(R.string.goals_name, modifier = Modifier.padding(top = CauriSpacing.s))
    CauriTextField(
        value = draft.name,
        onValueChange = { onChange(draft.copy(name = it, nameMissing = false)) },
        missing = draft.nameMissing,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next,
        ),
        placeholder = stringResource(
            when (draft.kind) {
                GoalKind.SAVING -> R.string.goals_name_saving_hint
                GoalKind.TONTINE -> R.string.goals_name_tontine_hint
                GoalKind.ENVELOPE -> R.string.goals_name_envelope_hint
            },
        ),
    )
    Label(
        when (draft.kind) {
            GoalKind.SAVING -> R.string.goals_amount_saving
            GoalKind.TONTINE -> R.string.goals_amount_tontine
            GoalKind.ENVELOPE -> R.string.goals_amount_envelope
        },
        modifier = Modifier.padding(top = CauriSpacing.s),
    )
    AmountField(
        value = draft.amount,
        missing = draft.amountMissing,
        onValueChange = { onChange(draft.copy(amount = it, amountMissing = false)) },
    )
}

// « Épargne », « Tontine », « Enveloppe ».
@Composable
private fun KindChips(draft: GoalDraft, onChange: (GoalDraft) -> Unit) {
    Chips(
        options = GoalKind.entries.map {
            it to stringResource(
                when (it) {
                    GoalKind.SAVING -> R.string.goals_kind_saving
                    GoalKind.TONTINE -> R.string.goals_kind_tontine
                    GoalKind.ENVELOPE -> R.string.goals_kind_envelope
                },
            )
        },
        selected = draft.kind,
        onSelect = { onChange(draft.copy(kind = it)) },
    )
}

@Composable
private fun CategoryChips(draft: GoalDraft, categories: List<Category>, onChange: (GoalDraft) -> Unit) {
    Label(R.string.goals_category, modifier = Modifier.padding(top = CauriSpacing.s))
    Chips(
        options = categories.map { it.id to it.name },
        selected = draft.categoryId,
        onSelect = { onChange(draft.copy(categoryId = it, categoryMissing = false)) },
    )
    if (draft.categoryMissing) {
        Text(
            text = stringResource(R.string.goals_category_missing),
            style = MaterialTheme.typography.bodySmall,
            color = CauriTheme.palette.coralText,
        )
    }
}

// « Jour de la cotisation » and its stepper.
@Composable
private fun DayRow(draft: GoalDraft, onChange: (GoalDraft) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(R.string.goals_contribution_day, modifier = Modifier.weight(1f))
        Stepper(
            value = draft.dayOfMonth,
            canLower = draft.dayOfMonth > 1,
            canRaise = draft.dayOfMonth < LAST_DAY,
            onStep = { onChange(draft.copy(dayOfMonth = draft.dayOfMonth + it)) },
        )
    }
}

// A date and its value, « Aucune » without one; a tap opens the dialog.
@Composable
private fun DateRow(@StringRes label: Int, date: LocalDate?, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    val locale = LocalConfiguration.current.locales[0]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(label, modifier = Modifier.weight(1f))
        Text(
            text = date?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
                ?: stringResource(R.string.goals_date_none),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (date == null) palette.muted else palette.accentText,
        )
    }
}

// « Mis de côté : 420 000 F », the money to move, « Verser » and « Reprendre ».
@Composable
private fun Moving(draft: GoalDraft, actions: GoalActions) {
    val palette = CauriTheme.palette
    Text(
        text = stringResource(R.string.goals_holds, formatFcfa(draft.saved)),
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        color = palette.goldText,
        modifier = Modifier.padding(top = CauriSpacing.s),
    )
    AmountField(value = draft.moving, missing = false, onValueChange = {
        actions.onDraftChange(draft.copy(moving = it))
    })
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        TextButton(onClick = { actions.onMove(putAside = true) }) {
            Text(text = stringResource(R.string.goals_put_in), color = palette.accentText)
        }
        TextButton(onClick = { actions.onMove(putAside = false) }) {
            Text(text = stringResource(R.string.goals_take_back), color = palette.muted)
        }
    }
}

// « Enregistrer », then « Mettre en premier » and « Supprimer » for a goal saved.
@Composable
private fun Answers(draft: GoalDraft, actions: GoalActions) {
    val palette = CauriTheme.palette
    Button(
        onClick = actions::onDraftSave,
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.l).height(ButtonHeight),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
    ) {
        Text(text = stringResource(R.string.goals_save), style = MaterialTheme.typography.titleMedium)
    }
    if (draft.id != null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (!draft.first) {
                TextButton(onClick = actions::onMoveFirst) {
                    Text(text = stringResource(R.string.goals_move_first), color = palette.accentText)
                }
            } else {
                Box {}
            }
            TextButton(onClick = actions::onDraftDelete) {
                Text(text = stringResource(R.string.goals_delete), color = palette.coralText)
            }
        }
    }
}

@Composable
private fun AmountField(value: String, missing: Boolean, onValueChange: (String) -> Unit) {
    CauriTextField(
        value = value,
        onValueChange = onValueChange,
        missing = missing,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        visualTransformation = FcfaVisualTransformation,
        placeholder = stringResource(R.string.goals_amount_hint),
        trailing = {
            Text(
                text = stringResource(R.string.goals_currency),
                style = MaterialTheme.typography.bodyLarge,
                color = CauriTheme.palette.muted,
            )
        },
    )
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
