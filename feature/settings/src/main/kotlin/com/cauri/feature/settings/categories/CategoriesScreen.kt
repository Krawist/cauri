package com.cauri.feature.settings.categories

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.ReorderableColumn
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.BackButton
import com.cauri.feature.settings.R
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val RowHeight = 64.dp
private val SubRowHeight = 44.dp
private val SegmentHeight = 36.dp
private val AddSize = 44.dp
private val SubIndent = 52.dp

/**
 * « Catégories »: the categories of the tab (« Dépenses », « Revenus », « Épargne »), each with its icon, its
 * sub-categories and what it counted this month; the chevron opens the sub-categories with their amounts and
 * « + Sous-catégorie », a tap opens the sheet, a long press moves it. « + » creates one, « Archivées » brings one back.
 */
@Composable
internal fun CategoriesScreen(onBack: () -> Unit, viewModel: CategoriesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesContent(state = state, actions = viewModel, onBack = onBack)
    state.draft?.let { CategorySheet(draft = it, state = state, actions = viewModel) }
    if (state.showingArchived) ArchivedDialog(archived = state.archived, actions = viewModel)
}

@Composable
private fun CategoriesContent(state: CategoriesUiState, actions: CategoriesActions, onBack: () -> Unit) {
    val tops = state.categories.filter { it.parentId == null && it.nature == state.nature }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
    ) {
        Header(state = state, onBack = onBack, onAdd = { actions.onEdit(null) })
        Tabs(state = state, onNature = actions::onNature)
        if (tops.isNotEmpty()) {
            GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
                ReorderableColumn(
                    items = tops,
                    key = { it.id },
                    step = RowHeight,
                    onReorder = { order -> actions.onReorder(order.map { it.id }) },
                ) { category, moving, drag ->
                    CategoryRow(category = category, state = state, moving = moving, drag = drag, actions = actions)
                }
            }
            Text(
                text = stringResource(R.string.category_list_hint),
                style = MaterialTheme.typography.bodySmall,
                color = CauriTheme.palette.muted,
                modifier = Modifier.padding(horizontal = CauriSpacing.xs),
            )
        }
        if (state.archived.isNotEmpty()) ArchivedRow(state = state, onClick = { actions.onShowArchived(true) })
    }
}

// The back button, « Catégories » over « Novembre · dépenses du mois », and the lagoon « + ».
@Composable
private fun Header(state: CategoriesUiState, onBack: () -> Unit, onAdd: () -> Unit) {
    val palette = CauriTheme.palette
    val locale = LocalConfiguration.current.locales[0]
    val month = state.month.format(DateTimeFormatter.ofPattern("MMMM", locale))
        .replaceFirstChar { it.titlecase(locale) }
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m), verticalAlignment = Alignment.CenterVertically) {
        BackButton(onClick = onBack)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = stringResource(R.string.category_list_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
            Text(
                text = stringResource(
                    when (state.nature) {
                        CategoryNature.EXPENSE -> R.string.category_list_month_expenses
                        CategoryNature.INCOME -> R.string.category_list_month_income
                        CategoryNature.SAVING -> R.string.category_list_month_saving
                    },
                    month,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
        val description = stringResource(R.string.category_list_new)
        Box(
            modifier = Modifier
                .size(AddSize)
                .background(palette.accent, CircleShape)
                .clickable(onClickLabel = description, role = Role.Button, onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_nav_add),
                contentDescription = description,
                tint = palette.onAccent,
            )
        }
    }
}

// « Dépenses · 9 », « Revenus · 2 », « Épargne · 2 »: the top-level categories of each kind.
@Composable
private fun Tabs(state: CategoriesUiState, onNature: (CategoryNature) -> Unit) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(CauriSpacing.xs)) {
            CategoryNature.entries.forEach { nature ->
                val chosen = nature == state.nature
                val count = state.categories.count { it.parentId == null && it.nature == nature }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(SegmentHeight)
                        .background(if (chosen) palette.segment else palette.segment.copy(alpha = 0f), CircleShape)
                        .clickable(role = Role.Tab) { onNature(nature) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(
                            when (nature) {
                                CategoryNature.EXPENSE -> R.string.category_list_tab_expenses
                                CategoryNature.INCOME -> R.string.category_list_tab_income
                                CategoryNature.SAVING -> R.string.category_list_tab_saving
                            },
                            count,
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = if (chosen) palette.ink else palette.muted,
                    )
                }
            }
        }
    }
}

// A category: its badge, name and sub-categories, its total and the chevron; open, its sub-categories under it.
@Composable
private fun CategoryRow(
    category: Category,
    state: CategoriesUiState,
    moving: Boolean,
    drag: Modifier,
    actions: CategoriesActions,
) {
    val palette = CauriTheme.palette
    val subs = state.categories.filter { it.parentId == category.id }
    val last = state.categories.lastOrNull { it.parentId == null && it.nature == category.nature }?.id == category.id
    val open = category.id in state.expanded && !moving
    val total = (state.totals[category.id] ?: 0) + subs.sumOf { state.totals[it.id] ?: 0 }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (moving) Modifier.background(palette.sheet.compositeOver(palette.background)) else Modifier),
    ) {
        Row(
            modifier = drag
                .fillMaxWidth()
                .height(RowHeight)
                .clickable(role = Role.Button) { actions.onEdit(category) }
                .padding(horizontal = CauriSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryBadge(category)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                SubsSummary(subs = subs, open = open)
            }
            Text(
                text = stringResource(R.string.category_list_amount, formatFcfa(total)),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Icon(
                painter = painterResource(DesignR.drawable.ic_chevron),
                contentDescription = stringResource(
                    if (open) R.string.category_list_close else R.string.category_list_open,
                ),
                tint = palette.muted,
                modifier = Modifier
                    .rotate(if (open) QUARTER_TURN else 0f)
                    .clickable(role = Role.Button) { actions.onToggle(category.id) },
            )
        }
        if (open) Subs(parent = category, subs = subs, state = state, actions = actions)
        if (!last) HorizontalDivider(color = palette.chip)
    }
}

// The names of the sub-categories, how many once they show under the row, or « Aucune sous-catégorie ».
@Composable
private fun SubsSummary(subs: List<Category>, open: Boolean) {
    Text(
        text = when {
            subs.isEmpty() -> stringResource(R.string.category_list_no_sub)
            open -> pluralStringResource(R.plurals.category_list_subs, subs.size, subs.size)
            else -> subs.joinToString { it.name }
        },
        style = MaterialTheme.typography.bodySmall,
        color = CauriTheme.palette.muted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

// The sub-categories of [parent] with their amounts, then « + Sous-catégorie », dashed.
@Composable
private fun Subs(parent: Category, subs: List<Category>, state: CategoriesUiState, actions: CategoriesActions) {
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier.padding(start = SubIndent + CauriSpacing.l, end = CauriSpacing.l, bottom = CauriSpacing.s),
    ) {
        subs.forEach { sub ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SubRowHeight)
                    .clickable(role = Role.Button) { actions.onEdit(sub) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sub.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = palette.ink,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.category_list_amount, formatFcfa(state.totals[sub.id] ?: 0)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            }
            HorizontalDivider(color = palette.chip)
        }
        Text(
            text = stringResource(R.string.category_list_add_sub),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = palette.muted,
            modifier = Modifier
                .padding(top = CauriSpacing.s)
                .border(1.5.dp, palette.handle, CircleShape)
                .clickable(role = Role.Button) { actions.onNewSub(parent) }
                .padding(horizontal = CauriSpacing.m, vertical = CauriSpacing.s),
        )
    }
}

// « Archivées · Santé, Éducation ».
@Composable
private fun ArchivedRow(state: CategoriesUiState, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.control),
        modifier = Modifier.fillMaxWidth(),
        interaction = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.category_list_archived,
                    state.archived.filter { it.parentId == null }.ifEmpty { state.archived }.joinToString { it.name },
                ),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(DesignR.drawable.ic_chevron),
                contentDescription = null,
                tint = palette.muted,
            )
        }
    }
}

private const val QUARTER_TURN = 90f
private const val PREVIEW_FOOD = 1L
private const val PREVIEW_MARKET_ID = 2L
private const val PREVIEW_TRANSPORT_ID = 3L
private const val PREVIEW_MARKET = 31_000L
private const val PREVIEW_TRANSPORT = 31_500L

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoriesContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            CategoriesContent(
                state = CategoriesUiState(
                    month = YearMonth.parse("2026-11"),
                    categories = listOf(
                        Category(id = 1, name = "Alimentation", nature = CategoryNature.EXPENSE),
                        Category(id = 2, name = "Marché", nature = CategoryNature.EXPENSE, parentId = 1),
                        Category(id = 3, name = "Transport", nature = CategoryNature.EXPENSE),
                    ),
                    totals = mapOf(PREVIEW_MARKET_ID to PREVIEW_MARKET, PREVIEW_TRANSPORT_ID to PREVIEW_TRANSPORT),
                    expanded = setOf(PREVIEW_FOOD),
                ),
                actions = PreviewCategoriesActions,
                onBack = {},
            )
        }
    }
}

/** Actions that do nothing, for previews. */
// One function per gesture of the screen and of its sheet.
@Suppress("TooManyFunctions")
private object PreviewCategoriesActions : CategoriesActions {
    override fun onNature(nature: CategoryNature) = Unit

    override fun onToggle(id: Long) = Unit

    override fun onEdit(category: Category?) = Unit

    override fun onNewSub(parent: Category) = Unit

    override fun onDraftChange(draft: CategoryDraft) = Unit

    override fun onDraftSave() = Unit

    override fun onAddSub() = Unit

    override fun onRemoveSub(id: Long) = Unit

    override fun onReorder(ids: List<Long>) = Unit

    override fun onAskMerge(open: Boolean) = Unit

    override fun onMerge(into: Long) = Unit

    override fun onArchive() = Unit

    override fun onShowArchived(open: Boolean) = Unit

    override fun onRestore(id: Long) = Unit

    override fun onEditClose() = Unit
}
