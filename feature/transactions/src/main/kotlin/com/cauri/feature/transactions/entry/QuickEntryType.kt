package com.cauri.feature.transactions.entry

/** What the quick entry records: « Dépense », « Revenu » or « Transfert ». */
internal enum class QuickEntryType {
    EXPENSE,
    INCOME,

    /** Money moved between two of the user's accounts. */
    TRANSFER,
}
