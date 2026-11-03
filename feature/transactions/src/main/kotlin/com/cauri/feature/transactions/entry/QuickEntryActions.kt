package com.cauri.feature.transactions.entry

/** What the user can do in the quick entry sheet. */
// One function per gesture of the sheet.
@Suppress("TooManyFunctions")
internal interface QuickEntryActions {
    /** Starts a new entry each time the sheet opens: an expense in cash. */
    fun onOpen()

    fun onType(type: QuickEntryType)

    /** Chooses the account the money leaves, or arrives on for an income. */
    fun onAccount(id: Long)

    /** Chooses the account a transfer arrives on. */
    fun onToAccount(id: Long)

    /**
     * Adds [digits], one digit or « 000 », to the amount; a leading zero is never kept, and an amount over
     * [QuickEntryViewModel.MAX_AMOUNT] is refused with a message.
     */
    fun onDigits(digits: String)

    /** Erases the last digit of the amount. */
    fun onDelete()

    /** Clears the whole amount: a long press on the erase key. */
    fun onClear()

    /** Chooses the category [id], or leaves the entry without category when it was chosen already. */
    fun onCategory(id: Long)

    /** Shows every category of the type after « Autre… ». */
    fun onShowAllCategories()

    /** Records the entry; the sheet then closes. */
    fun onSave()

    /** The sheet closed after recording: the next one starts afresh. */
    fun onSavedShown()

    /** Puts the entry and the operation it was merged into apart again. */
    fun onUndoMerge()

    /** The time to undo the merge is over. */
    fun onMergeShown()
}
