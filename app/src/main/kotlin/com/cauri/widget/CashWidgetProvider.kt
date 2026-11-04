package com.cauri.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** « Cash dans ta poche » on the home screen: the cash, what is available today, and « + » for the quick entry. */
@AndroidEntryPoint
internal class CashWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var updater: CashWidgetUpdater

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        // The app's start follows the figures; a widget just added asks for them once.
        updater.start()
        updater.refresh()
    }
}
