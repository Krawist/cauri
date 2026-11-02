package com.cauri.core.data.model

/** How the user's income arrives: budgets only split money that has arrived either way. */
enum class IncomeType {
    /** A salary on a fixed date each month. */
    FIXED_DATE,

    /** Amounts and dates that change from month to month. */
    VARIABLE,
}
