package com.cauri.feature.home

import com.cauri.core.data.model.Habit

/** What the user can do with the one-tap cash expenses of the home screen. */
internal interface HabitActions {
    /** Records the habit as a confirmed cash expense, now. */
    fun onHabitTap(habit: Habit)

    /** Opens the habit sheet on [habit], or on a new habit when `null`. */
    fun onEditHabit(habit: Habit?)

    /** Deletes the expense just recorded. */
    fun onUndo()

    /** The time to undo is over. */
    fun onUndoDismissed()

    fun onDraftChange(draft: HabitDraft)

    /** Saves the habit of the sheet, or points out what is missing. */
    fun onDraftSave()

    /** Deletes the habit of the sheet. */
    fun onDraftDelete()

    fun onEditClose()

    /** Opens or closes the list to reorder the habits. */
    fun onManageHabits(open: Boolean)

    /** Puts the habits in the order of [ids]. */
    fun onReorder(ids: List<Long>)
}
