package com.cauri.core.data.model

/** Role of a place for the reminders. */
enum class PlaceKind {
    /** A shop or market, where a visit may mean an expense. */
    SHOP,

    /** Never triggers a reminder. */
    HOME,

    /** Never triggers a reminder. */
    WORK,
}
