package com.cauri.core.design.theme

import androidx.compose.ui.unit.dp

/** Corner radii of the mockups, from the largest surface to the smallest. Pills use `CircleShape`. */
object CauriRadius {
    /** Main card of a screen, such as the cash card of the home screen. */
    val hero = 28.dp

    /** Cards: cards to confirm, totals, sheets. */
    val card = 24.dp

    /** Grouped lists and segmented controls. */
    val group = 22.dp

    /** Small cards: statistics, the available amount. */
    val tile = 20.dp

    /** Controls: chips, one-tap habits, note tiles. */
    val control = 18.dp

    /** Icon boxes in front of a list row. */
    val icon = 14.dp

    /** Text fields and amount inputs. */
    val field = 12.dp
}
