package com.cauri.feature.places

/**
 * A pin on the [MapArea], placed by its share of the area's width and height.
 *
 * @property x Share of the width from the left, from 0 to 1.
 * @property y Share of the height from the top, from 0 to 1.
 * @property highlighted Whether it is drawn in gold, as a shop, rather than muted.
 */
internal data class MapPin(val x: Float, val y: Float, val highlighted: Boolean)
