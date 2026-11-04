package com.cauri.core.design.component

/** How a row of a [ReorderableColumn] is picked up. */
enum class ReorderPickUp {
    /** As soon as the finger moves on what listens: a handle. */
    TOUCH,

    /** After a long press: a whole row, whose tap does something else. */
    LONG_PRESS,
}
