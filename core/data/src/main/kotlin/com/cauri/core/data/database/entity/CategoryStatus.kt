package com.cauri.core.data.database.entity

/** Whether a category is offered to the user. */
internal enum class CategoryStatus {
    ACTIVE,

    /** Hidden from the choices; its transactions keep it. */
    ARCHIVED,
}
