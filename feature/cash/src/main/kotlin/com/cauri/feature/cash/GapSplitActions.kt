package com.cauri.feature.cash

import com.cauri.core.data.model.Category

/** What the user can do on the gap split screen. */
internal interface GapSplitActions {
    fun onAmountChange(key: String, amount: String)

    /** Opens or closes the category list of « + Autre dépense ». */
    fun onPickCategory(open: Boolean)

    /** Adds an expense in [category]. */
    fun onAddExpense(category: Category)

    /** Saves the typed expenses, the rest as « Divers cash », then the count. */
    fun onSave()
}
