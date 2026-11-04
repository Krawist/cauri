package com.cauri.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.GoalKind
import com.cauri.core.data.model.GoalProgress
import com.cauri.core.data.model.NewGoal
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.GoalRepository
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

/** « Objectifs »: the goals, the emergency fund created the first time, and the sheet of a goal. */
@HiltViewModel
internal class GoalsViewModel @Inject constructor(
    private val goals: GoalRepository,
    categories: CategoryRepository,
    clock: Clock,
) : ViewModel(),
    GoalActions {
    private val draft = MutableStateFlow<GoalDraft?>(null)
    private val month = YearMonth.now(clock)

    val uiState: StateFlow<GoalsUiState> = combine(goals.overview, categories.categories, draft) {
            overview,
            all,
            draft,
        ->
        GoalsUiState(
            month = month,
            overview = overview,
            categories = all.filter { it.nature == CategoryNature.EXPENSE },
            draft = draft,
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = GoalsUiState(month = month))

    /** Creates the emergency fund named [name] the first time « Objectifs » opens. */
    fun onShown(name: String) {
        viewModelScope.launch { goals.createEmergencyFund(name) }
    }

    override fun onEdit(progress: GoalProgress?) {
        val first = uiState.value.overview?.goals?.firstOrNull()?.goal?.id
        draft.value = progress?.let {
            val goal = it.goal
            GoalDraft(
                id = goal.id,
                kind = goal.kind,
                name = goal.name,
                amount = goal.amount.toString(),
                deadline = goal.deadline,
                payoutDate = goal.payoutDate,
                dayOfMonth = goal.dayOfMonth ?: 1,
                categoryId = goal.categoryId,
                saved = if (goal.kind == GoalKind.SAVING) it.saved else 0,
                first = goal.id == first,
            )
        } ?: GoalDraft()
    }

    override fun onDraftChange(draft: GoalDraft) {
        this.draft.value = draft.copy(
            amount = digits(draft.amount),
            moving = digits(draft.moving),
            dayOfMonth = draft.dayOfMonth.coerceIn(1, LAST_DAY),
        )
    }

    override fun onDraftSave() {
        val current = draft.value ?: return
        val amount = current.amount.toLongOrNull() ?: 0
        val categoryMissing = current.kind == GoalKind.ENVELOPE && current.categoryId == null
        if (current.name.isBlank() || amount <= 0 || categoryMissing) {
            draft.update {
                it?.copy(
                    nameMissing = it.name.isBlank(),
                    amountMissing = amount <= 0,
                    categoryMissing = categoryMissing,
                )
            }
            return
        }
        draft.value = null
        val goal = NewGoal(
            name = current.name.trim(),
            kind = current.kind,
            amount = amount,
            deadline = current.deadline,
            payoutDate = current.payoutDate,
            dayOfMonth = current.dayOfMonth,
            categoryId = current.categoryId,
        )
        viewModelScope.launch {
            val id = current.id
            if (id == null) goals.add(goal) else goals.update(id, goal)
        }
    }

    override fun onMove(putAside: Boolean) {
        val current = draft.value
        val id = current?.id
        val amount = current?.moving?.toLongOrNull()?.takeIf { it > 0 }
        if (id == null || amount == null) return
        // Never more taken back than the saving holds.
        val moved = if (putAside) amount else -minOf(amount, current.saved)
        draft.value = current.copy(saved = current.saved + moved, moving = "")
        viewModelScope.launch { goals.putAside(id, moved) }
    }

    override fun onMoveFirst() {
        val id = draft.value?.id ?: return
        draft.update { it?.copy(first = true) }
        viewModelScope.launch { goals.moveFirst(id) }
    }

    override fun onDraftDelete() {
        val id = draft.value?.id ?: return
        draft.value = null
        viewModelScope.launch { goals.delete(id) }
    }

    override fun onEditClose() {
        draft.value = null
    }

    // Digits only, without the zeros in front, up to 99 999 999 F.
    private fun digits(text: String) = text.filter(Char::isDigit).trimStart('0').take(MAX_AMOUNT_DIGITS)

    private companion object {
        const val MAX_AMOUNT_DIGITS = 8
        const val LAST_DAY = 31
    }
}
