package com.cauri.feature.settings.categories

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.ReorderPickUp
import com.cauri.core.design.component.ReorderableColumn
import com.cauri.core.design.format.formatFcfa
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.R

private val ButtonHeight = 56.dp
private val ActionHeight = 44.dp
private val IconChoiceSize = 48.dp
private val SwatchSize = 34.dp
private val SubRowHeight = 46.dp
private val TextOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done)

/**
 * The sheet of a category: its badge over « Dépense · 31 500 F ce mois », its name, icon and colour, its
 * sub-categories (added, removed, reordered by their handles), « Fusionner avec… », « Archiver » and « Enregistrer ».
 * A new category or a sub-category has no sub-categories, nor « Fusionner » and « Archiver » for a new one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategorySheet(draft: CategoryDraft, state: CategoriesUiState, actions: CategoriesActions) {
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
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.l),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.l),
        ) {
            SheetHeader(draft = draft, state = state)
            NameField(draft = draft, onChange = actions::onDraftChange)
            IconChoices(draft = draft, onChange = actions::onDraftChange)
            ColorChoices(draft = draft, onChange = actions::onDraftChange)
            if (draft.id != null && draft.parentId == null) {
                SubCategories(draft = draft, state = state, actions = actions)
            }
            if (draft.id != null) MergeAndArchive(draft = draft, state = state, actions = actions)
            Button(
                onClick = actions::onDraftSave,
                modifier = Modifier.fillMaxWidth().height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.category_list_save), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// The badge, « Dépense · 31 500 F ce mois », and the name as typed.
@Composable
private fun SheetHeader(draft: CategoryDraft, state: CategoriesUiState) {
    val palette = CauriTheme.palette
    val total = draft.id?.let { id ->
        (state.totals[id] ?: 0) + state.categories.filter { it.parentId == id }.sumOf { state.totals[it.id] ?: 0 }
    } ?: 0
    Row(
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryBadge(icon = draft.icon, color = draft.color, size = 56.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(
                    R.string.category_list_sheet_total,
                    stringResource(kindOf(draft.nature)),
                    formatFcfa(total),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
            Text(
                text = draft.name.ifBlank {
                    stringResource(
                        if (draft.parentId == null) R.string.category_list_new else R.string.category_list_new_sub,
                    )
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
        }
    }
}

@Composable
private fun NameField(draft: CategoryDraft, onChange: (CategoryDraft) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Label(R.string.category_list_name)
        CauriTextField(
            value = draft.name,
            onValueChange = { onChange(draft.copy(name = it, nameMissing = false)) },
            missing = draft.nameMissing,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            keyboardOptions = TextOptions,
            placeholder = stringResource(R.string.category_list_name_hint),
        )
    }
}

// The icons, the chosen one framed in its colour.
@Composable
private fun IconChoices(draft: CategoryDraft, onChange: (CategoryDraft) -> Unit) {
    val palette = CauriTheme.palette
    val (tint, background) = shadesOf(draft.color)
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Label(R.string.category_list_icon)
        CategoryIcon.entries.chunked(ICONS_A_LINE).forEach { line ->
            // One cell each, the last line keeping the columns of the first.
            Row(modifier = Modifier.fillMaxWidth()) {
                line.forEach { icon ->
                    val chosen = icon == draft.icon
                    val shape = RoundedCornerShape(CauriRadius.control)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(iconOf(icon)),
                            contentDescription = null,
                            tint = if (chosen) tint else palette.ink,
                            modifier = Modifier
                                .size(IconChoiceSize)
                                .background(if (chosen) background else palette.chip, shape)
                                .then(if (chosen) Modifier.border(2.dp, swatchOf(draft.color), shape) else Modifier)
                                .clickable(role = Role.RadioButton) { onChange(draft.copy(icon = icon)) }
                                .padding(CauriSpacing.m),
                        )
                    }
                }
                repeat(ICONS_A_LINE - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// The five colours of the palette, the chosen one ringed.
@Composable
private fun ColorChoices(draft: CategoryDraft, onChange: (CategoryDraft) -> Unit) {
    val palette = CauriTheme.palette
    val description = stringResource(R.string.category_list_color)
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Label(R.string.category_list_color)
        Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m)) {
            CategoryColor.entries.forEach { color ->
                val chosen = color == draft.color
                Row(
                    modifier = Modifier
                        .size(SwatchSize)
                        .background(swatchOf(color), CircleShape)
                        .then(if (chosen) Modifier.border(3.dp, palette.ink, CircleShape) else Modifier)
                        .clickable(role = Role.RadioButton) { onChange(draft.copy(color = color)) }
                        .semantics { contentDescription = description },
                ) {}
            }
        }
    }
}

// The sub-categories, each with its handle and its cross, then the field of a new one.
@Composable
private fun SubCategories(draft: CategoryDraft, state: CategoriesUiState, actions: CategoriesActions) {
    val palette = CauriTheme.palette
    val subs = state.categories.filter { it.parentId == draft.id }
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Label(R.string.category_list_subs_label)
        if (subs.isNotEmpty()) {
            GlassSurface(shape = RoundedCornerShape(CauriRadius.control), modifier = Modifier.fillMaxWidth()) {
                ReorderableColumn(
                    items = subs,
                    key = { it.id },
                    step = SubRowHeight,
                    onReorder = { order -> actions.onReorder(order.map { it.id }) },
                    pickUp = ReorderPickUp.TOUCH,
                ) { sub, moving, drag ->
                    SubRow(sub = sub, moving = moving, drag = drag, last = sub.id == subs.last().id, actions = actions)
                }
            }
        }
        CauriTextField(
            value = draft.newSub,
            onValueChange = { actions.onDraftChange(draft.copy(newSub = it)) },
            missing = false,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = TextOptions,
            keyboardActions = KeyboardActions(onDone = { actions.onAddSub() }),
            placeholder = stringResource(R.string.category_list_add_sub_hint),
            trailing = {
                Text(
                    text = stringResource(R.string.category_list_add),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.accentText,
                    modifier = Modifier.clickable(role = Role.Button) { actions.onAddSub() }.padding(CauriSpacing.s),
                )
            },
        )
    }
}

@Composable
private fun SubRow(sub: Category, moving: Boolean, drag: Modifier, last: Boolean, actions: CategoriesActions) {
    val palette = CauriTheme.palette
    Column(
        modifier = if (moving) Modifier.background(palette.sheet.compositeOver(palette.background)) else Modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(SubRowHeight).padding(horizontal = CauriSpacing.m),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_drag_lines),
                contentDescription = stringResource(R.string.category_list_move),
                tint = palette.muted,
                modifier = drag,
            )
            Text(
                text = sub.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = palette.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "×",
                style = MaterialTheme.typography.titleMedium,
                color = palette.muted,
                modifier = Modifier
                    .clickable(
                        onClickLabel = stringResource(R.string.category_list_remove, sub.name),
                        role = Role.Button,
                    ) { actions.onRemoveSub(sub.id) }
                    .padding(horizontal = CauriSpacing.s),
            )
        }
        if (!last) HorizontalDivider(color = palette.chip)
    }
}

// « Fusionner avec… » and « Archiver », and what archiving keeps.
@Composable
private fun MergeAndArchive(draft: CategoryDraft, state: CategoriesUiState, actions: CategoriesActions) {
    val palette = CauriTheme.palette
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
            TextButton(
                onClick = { actions.onAskMerge(true) },
                modifier = Modifier.weight(1f).height(ActionHeight).background(palette.chip, CircleShape),
            ) { Text(stringResource(R.string.category_list_merge), color = palette.ink) }
            TextButton(
                onClick = actions::onArchive,
                modifier = Modifier.weight(1f).height(ActionHeight).background(palette.chip, CircleShape),
            ) { Text(stringResource(R.string.category_list_archive), color = palette.coralText) }
        }
        Text(
            text = stringResource(R.string.category_list_archive_hint),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
        )
    }
    if (state.merging) MergeDialog(draft = draft, state = state, actions = actions)
}

@Composable
private fun Label(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = CauriTheme.palette.muted,
    )
}

/** « Dépense », « Revenu » or « Épargne ». */
@StringRes
internal fun kindOf(nature: CategoryNature): Int = when (nature) {
    CategoryNature.EXPENSE -> R.string.category_list_kind_expense
    CategoryNature.INCOME -> R.string.category_list_kind_income
    CategoryNature.SAVING -> R.string.category_list_kind_saving
}

private const val ICONS_A_LINE = 6
