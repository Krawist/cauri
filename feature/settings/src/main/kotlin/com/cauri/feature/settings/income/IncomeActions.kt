package com.cauri.feature.settings.income

import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.FixedCostSuggestion

/** What the user can do on « Répartir mes revenus ». */
internal interface IncomeActions {
    fun onBack()

    /** Moves the savings rate by [step] times 5 %. */
    fun onSavingsRate(step: Int)

    /** Moves the smallest income split by [step] times 5 000 F. */
    fun onThreshold(step: Int)

    /** Makes the suggestion a fixed cost. */
    fun onConfirm(suggestion: FixedCostSuggestion)

    /** Refuses the suggestion: Cauri asks about it no more. */
    fun onIgnore(suggestion: FixedCostSuggestion)

    /** Opens the sheet of [cost], or of a new fixed cost when `null`. */
    fun onEdit(cost: FixedCost?)

    fun onDraftChange(draft: FixedCostDraft)

    fun onDraftSave()

    fun onDraftDelete()

    fun onEditClose()
}
