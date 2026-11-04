package com.cauri.feature.settings.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.NewCategory
import com.cauri.core.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** « Catégories »: the categories of each kind with what they counted this month, and the sheet of one. */
// One function per gesture of the screen and of its sheet.
@Suppress("TooManyFunctions")
@HiltViewModel
internal class CategoriesViewModel @Inject constructor(
    private val repository: CategoryRepository,
    clock: Clock,
) : ViewModel(),
    CategoriesActions {
    private val local = MutableStateFlow(CategoriesUiState(month = YearMonth.now(clock)))

    val uiState: StateFlow<CategoriesUiState> = combine(
        local,
        repository.categories,
        repository.archived,
        repository.monthTotals,
    ) { state, categories, archived, totals ->
        state.copy(categories = categories, archived = archived, totals = totals)
    }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = local.value)

    override fun onNature(nature: CategoryNature) = local.update { it.copy(nature = nature) }

    override fun onToggle(id: Long) = local.update {
        it.copy(expanded = if (id in it.expanded) it.expanded - id else it.expanded + id)
    }

    override fun onEdit(category: Category?) = local.update { state ->
        state.copy(
            draft = category?.let {
                CategoryDraft(
                    id = it.id,
                    parentId = it.parentId,
                    nature = it.nature,
                    name = it.name,
                    icon = it.icon,
                    color = it.color,
                )
            } ?: CategoryDraft(nature = state.nature),
        )
    }

    override fun onNewSub(parent: Category) = local.update {
        it.copy(
            draft = CategoryDraft(
                parentId = parent.id,
                nature = parent.nature,
                icon = parent.icon,
                color = parent.color,
            ),
        )
    }

    override fun onDraftChange(draft: CategoryDraft) = local.update { it.copy(draft = draft) }

    /** Saves the category of the sheet, or points out its name missing. */
    override fun onDraftSave() {
        val draft = local.value.draft ?: return
        if (draft.name.isBlank()) {
            local.update { it.copy(draft = draft.copy(nameMissing = true)) }
            return
        }
        local.update { it.copy(draft = null) }
        viewModelScope.launch {
            val id = draft.id
            if (id == null) {
                repository.createCategories(
                    listOf(
                        NewCategory(
                            name = draft.name,
                            nature = draft.nature,
                            parentId = draft.parentId,
                            icon = draft.icon,
                            color = draft.color,
                        ),
                    ),
                )
            } else {
                repository.update(id = id, name = draft.name, icon = draft.icon, color = draft.color)
            }
        }
    }

    /** Adds the sub-category typed to the category of the sheet. */
    override fun onAddSub() {
        val draft = local.value.draft
        val parent = draft?.id
        val name = draft?.newSub?.trim().orEmpty()
        if (parent == null || name.isEmpty()) return
        local.update { it.copy(draft = draft.copy(newSub = "")) }
        viewModelScope.launch {
            repository.createCategories(
                listOf(
                    NewCategory(
                        name = name,
                        nature = draft.nature,
                        parentId = parent,
                        icon = draft.icon,
                        color = draft.color,
                    ),
                ),
            )
        }
    }

    /** Removes the sub-category [id]: archived, its history kept. */
    override fun onRemoveSub(id: Long) {
        viewModelScope.launch { repository.archive(id) }
    }

    override fun onReorder(ids: List<Long>) {
        viewModelScope.launch { repository.reorder(ids) }
    }

    override fun onAskMerge(open: Boolean) = local.update { it.copy(merging = open) }

    /** Merges the category of the sheet into [into] and closes the sheet. */
    override fun onMerge(into: Long) {
        val from = local.value.draft?.id ?: return
        local.update { it.copy(draft = null, merging = false) }
        viewModelScope.launch { repository.merge(from = from, into = into) }
    }

    /** Archives the category of the sheet and closes it. */
    override fun onArchive() {
        val id = local.value.draft?.id ?: return
        local.update { it.copy(draft = null) }
        viewModelScope.launch { repository.archive(id) }
    }

    override fun onShowArchived(open: Boolean) = local.update { it.copy(showingArchived = open) }

    override fun onRestore(id: Long) {
        viewModelScope.launch { repository.restore(id) }
    }

    override fun onEditClose() = local.update { it.copy(draft = null, merging = false) }
}
