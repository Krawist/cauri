package com.cauri.core.data.database.entity

/** Whether a fixed cost holds or the user said a suggested one is none. */
internal enum class FixedCostStatus {
    /** Typed or confirmed by the user: it holds. */
    ACTIVE,

    /** Suggested and refused: Cauri suggests it no more. */
    IGNORED,
}
