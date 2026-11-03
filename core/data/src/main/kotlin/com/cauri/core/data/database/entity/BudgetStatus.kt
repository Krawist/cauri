package com.cauri.core.data.database.entity

/** Whether the budget of a category holds or the user ignored its suggestion. */
internal enum class BudgetStatus {
    /** Accepted or typed by the user: it holds. */
    ACTIVE,

    /** The suggestion was ignored: Cauri suggests none again for the category. */
    IGNORED,
}
