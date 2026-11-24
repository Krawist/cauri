package com.cauri.feature.onboarding.categories

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.NewCategory
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.dashedBorder
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.ChoiceSurface
import com.cauri.feature.onboarding.FirstHabit
import com.cauri.feature.onboarding.MissingAnswer
import com.cauri.feature.onboarding.OnboardingStep
import com.cauri.feature.onboarding.R

private const val CATEGORIES_STEP = 3
private val ChipHeight = 44.dp

/**
 * Category selection step of the onboarding: [onCategoriesChosen] receives the categories to create and the first
 * habits of the kept ones, named in the user's language.
 */
@Composable
internal fun CategoriesScreen(
    onCategoriesChosen: (categories: List<NewCategory>, habits: List<FirstHabit>) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // The resources of the current configuration: the names are saved in the language the user sees.
    val resources = LocalResources.current
    LaunchedEffect(state.answered) {
        state.answered?.let { answers ->
            onCategoriesChosen(
                answers.pack.map { NewCategory(name = resources.getString(it.label), nature = it.nature) } +
                    answers.added.map { NewCategory(name = it, nature = CategoryNature.EXPENSE) },
                answers.habits.map {
                    FirstHabit(
                        name = resources.getString(it.label),
                        amount = it.amount,
                        categoryName = resources.getString(it.category.label),
                    )
                },
            )
            viewModel.onAnswersHandedOn()
        }
    }
    CategoriesContent(state = state, actions = viewModel)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesContent(state: CategoriesUiState, actions: CategoriesActions) {
    val keptCount = state.keptPack.size + state.added.size
    OnboardingStep(
        step = CATEGORIES_STEP,
        title = stringResource(R.string.categories_title),
        subtitle = stringResource(R.string.categories_subtitle),
        continueLabel = pluralStringResource(R.plurals.categories_continue, keptCount, keptCount),
        onContinue = actions::onContinue,
        complete = state.complete,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
        ) {
            PackCategory.entries.forEach { category ->
                CategoryChip(
                    label = stringResource(category.label),
                    kept = category in state.keptPack,
                    saving = category.nature == CategoryNature.SAVING,
                    onToggle = { actions.onPackCategoryToggle(category) },
                )
            }
            state.added.forEach { name ->
                CategoryChip(label = name, kept = true, saving = false, onToggle = {
                    actions.onAddedCategoryRemove(name)
                })
            }
            if (!state.adding) AddChip(onClick = actions::onAddStart)
        }
        if (state.adding) {
            NewCategoryField(
                name = state.newName,
                onNameChange = actions::onNewNameChange,
                onConfirm = actions::onAddConfirm,
            )
        }
        if (state.noneKept) MissingAnswer(R.string.categories_none_kept)
        TontineNote()
    }
}

// Kept categories are lagoon; the tontine is gold, the colour of savings.
@Composable
private fun CategoryChip(label: String, kept: Boolean, saving: Boolean, onToggle: () -> Unit) {
    val palette = CauriTheme.palette
    val keptColor = if (saving) palette.gold else palette.accent
    ChoiceSurface(
        selected = kept,
        onSelect = onToggle,
        shape = CircleShape,
        modifier = Modifier.height(ChipHeight),
        selectedColor = keptColor,
        selectedFill = if (saving) palette.goldSoft else palette.accentSoft,
        role = Role.Checkbox,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (kept) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = when {
                kept && saving -> palette.goldText
                kept -> palette.accentText
                else -> palette.ink
            },
            modifier = Modifier.align(Alignment.Center).padding(horizontal = CauriSpacing.l),
        )
    }
}

@Composable
private fun AddChip(onClick: () -> Unit) {
    val palette = CauriTheme.palette
    Box(
        modifier = Modifier
            .height(ChipHeight)
            .clip(CircleShape)
            .dashedBorder(palette.handle)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.categories_add),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = palette.muted,
            modifier = Modifier.padding(horizontal = CauriSpacing.l),
        )
    }
}

@Composable
private fun NewCategoryField(name: String, onNameChange: (String) -> Unit, onConfirm: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            CauriTextField(
                value = name,
                onValueChange = onNameChange,
                missing = false,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                placeholder = stringResource(R.string.categories_new_name),
                autoFocus = true,
            )
        }
        TextButton(onClick = onConfirm) {
            Text(
                text = stringResource(R.string.categories_add_confirm),
                style = MaterialTheme.typography.labelLarge,
                color = CauriTheme.palette.accentText,
            )
        }
    }
}

@Composable
private fun TontineNote() {
    val palette = CauriTheme.palette
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.tile),
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.xl),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        ) {
            Icon(painter = painterResource(R.drawable.ic_info), contentDescription = null, tint = palette.goldText)
            Text(
                text = stringResource(R.string.categories_tontine_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoriesContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            CategoriesContent(
                state = CategoriesUiState(added = listOf("Moto")),
                actions = object : CategoriesActions {
                    override fun onPackCategoryToggle(category: PackCategory) = Unit

                    override fun onAddedCategoryRemove(name: String) = Unit

                    override fun onAddStart() = Unit

                    override fun onNewNameChange(name: String) = Unit

                    override fun onAddConfirm() = Unit

                    override fun onContinue() = Unit
                },
            )
        }
    }
}
