package com.cauri.core.data.database.entity

/** Role of a place for the reminders. */
internal enum class PlaceKind {
    /** A shop or market, where a visit may mean an expense. */
    SHOP,

    /** Never triggers a reminder. */
    HOME,

    /** Never triggers a reminder. */
    WORK,
}
