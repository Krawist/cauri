package com.cauri.core.capture.permission

/** How far the user lets Cauri use the location, for place reminders. */
enum class LocationAccess {
    /** No location at all: place reminders are off. */
    DENIED,

    /** Only while Cauri is open: not enough to notice a visit, which happens with the app closed. */
    WHILE_IN_USE,

    /** Also in the background: place reminders can work. */
    ALWAYS,
}
