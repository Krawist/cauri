package com.cauri.core.data.model

/** What a category counts: spending, income or money put aside. */
enum class CategoryNature {
    EXPENSE,
    INCOME,

    /** Savings, such as the tontine: never counted as spending. */
    SAVING,
}
