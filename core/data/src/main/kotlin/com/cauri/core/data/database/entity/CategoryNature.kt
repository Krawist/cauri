package com.cauri.core.data.database.entity

/** What a category counts: spending, income or money put aside. */
internal enum class CategoryNature {
    EXPENSE,
    INCOME,

    /** Savings, such as the tontine: never counted as spending. */
    SAVING,
}
