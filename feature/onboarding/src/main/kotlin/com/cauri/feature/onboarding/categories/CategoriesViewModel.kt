package com.cauri.feature.onboarding.categories

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Category selection step of the onboarding: categories of the pack to keep or remove, and the user's own. */
@HiltViewModel
internal class CategoriesViewModel @Inject constructor() :
    ViewModel(),
    CategoriesActions {
    private val state = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = state.asStateFlow()

    // The missing choice is only pointed out once the user tried to continue, then follows what they keep.
    private var continueAttempted = false

    override fun onPackCategoryToggle(category: PackCategory) = updateChoices {
        it.copy(keptPack = if (category in it.keptPack) it.keptPack - category else it.keptPack + category)
    }

    override fun onAddedCategoryRemove(name: String) = updateChoices { it.copy(added = it.added - name) }

    override fun onAddStart() = state.update { it.copy(adding = true) }

    override fun onNewNameChange(name: String) = state.update { it.copy(newName = name) }

    override fun onAddConfirm() = updateChoices { current ->
        val name = current.newName.trim()
        val alreadyAdded = current.added.any { it.equals(name, ignoreCase = true) }
        val added = if (name.isEmpty() || alreadyAdded) current.added else current.added + name
        current.copy(added = added, newName = "", adding = false)
    }

    override fun onContinue() {
        continueAttempted = true
        state.update { current ->
            val pack = PackCategory.entries.filter { it in current.keptPack }
            val noneKept = noneKept(current)
            current.copy(noneKept = noneKept, answered = if (noneKept) null else CategoriesAnswers(pack, current.added))
        }
    }

    private fun updateChoices(change: (CategoriesUiState) -> CategoriesUiState) = state.update { current ->
        val changed = change(current)
        val noneKept = noneKept(changed)
        changed.copy(complete = !noneKept, noneKept = if (continueAttempted) noneKept else changed.noneKept)
    }

    private fun noneKept(choices: CategoriesUiState) = choices.keptPack.isEmpty() && choices.added.isEmpty()
}
