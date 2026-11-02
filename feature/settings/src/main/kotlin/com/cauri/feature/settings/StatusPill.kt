package com.cauri.feature.settings

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

/**
 * Status of a permission as shown at the end of its row.
 *
 * @property label What the status says (« Autorisé », « Désactivée »…).
 * @property fill Fill of the pill.
 * @property color Colour of the label.
 */
internal data class StatusPill(@param:StringRes val label: Int, val fill: Color, val color: Color)
