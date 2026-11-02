package com.cauri.core.design.component

import androidx.annotation.DrawableRes

/**
 * One tab of the [CauriBottomBar].
 *
 * @property icon Drawable of the icon, tinted by the bar.
 * @property label Name of the tab, read by accessibility services (the bar shows icons only).
 */
data class BottomBarTab(@param:DrawableRes val icon: Int, val label: String)
