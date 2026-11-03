package com.cauri.feature.cash

/** What the user can do on the cash count screen. */
internal interface CashCountActions {
    fun onAdd(denomination: Denomination)

    fun onRemove(denomination: Denomination)

    /** « Tout effacer »: no banknote, no coin, no typed total. */
    fun onClearAll()

    /** Switches between counting banknotes and coins and typing the total. */
    fun onTypeTotal(typing: Boolean)

    fun onTypedTotalChange(total: String)

    /** Saves the count, or leads to the split of the missing money. */
    fun onConfirm()
}
