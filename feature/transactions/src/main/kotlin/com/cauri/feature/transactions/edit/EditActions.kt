package com.cauri.feature.transactions.edit

import java.time.Instant

/** What the user does in the edit sheet. */
// One function per gesture of the sheet.
@Suppress("TooManyFunctions")
internal interface EditActions {
    /** Keeps [digits] as the whole amount, in FCFA; refused over the largest amount. */
    fun onAmount(digits: String)

    /** Chooses the category [id], or leaves the transaction without one when it was already chosen. */
    fun onCategory(id: Long)

    /** Shows every category of the type: « Toutes… ». */
    fun onShowAllCategories()

    /** Moves the transaction to the account [id]; only for one Cauri did not read in a message. */
    fun onAccount(id: Long)

    /** Keeps [moment] as the time the money moved. */
    fun onOccurredAt(moment: Instant)

    /** Keeps [note] as the free note. */
    fun onNote(note: String)

    /** Adds a part to split to another category. */
    fun onAddPart()

    /** Chooses the category [categoryId] for the part at [index]. */
    fun onPartCategory(index: Int, categoryId: Long)

    /** Keeps [digits] as the amount of the part at [index], in FCFA. */
    fun onPartAmount(index: Int, digits: String)

    /** Removes the part at [index]. */
    fun onRemovePart(index: Int)

    /** Confirms the transaction as edited: « Valider ». */
    fun onConfirm()

    /** Sets the transaction aside: « Rien dépensé » or « Ignorer ». */
    fun onDismiss()

    /** Undoes the merge with an entry typed, or the link of two messages into a transfer: « Séparer ». */
    fun onSeparate()
}
