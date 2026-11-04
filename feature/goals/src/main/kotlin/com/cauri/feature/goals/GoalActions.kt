package com.cauri.feature.goals

import com.cauri.core.data.model.GoalProgress

/** What the user can do on « Objectifs ». */
internal interface GoalActions {
    /** Opens the sheet of [progress]'s goal, or of a new goal when `null`. */
    fun onEdit(progress: GoalProgress?)

    fun onDraftChange(draft: GoalDraft)

    fun onDraftSave()

    /** Puts the money typed aside for the saving in the sheet, or takes it back when not [putAside]. */
    fun onMove(putAside: Boolean)

    /** Puts the goal in the sheet first: the next savings go to it. */
    fun onMoveFirst()

    fun onDraftDelete()

    fun onEditClose()
}
