package com.cauri.widget

/** Keeps the home screen widgets showing the cash in pocket and what is available today. */
internal interface CashWidgetUpdater {
    /** Follows the cash and the available amount for as long as the app lives, redrawing the widgets on each change. */
    fun start()

    /** Redraws the widgets now with the latest figures, such as when one is added. */
    fun refresh()
}
