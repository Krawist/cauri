package com.cauri.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.cauri.R
import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.repository.AvailableRepository
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.design.format.formatFcfa
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Default implementation for [CashWidgetUpdater]. */
@Singleton
internal class CashWidgetUpdaterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cashCounts: CashCountRepository,
    private val available: AvailableRepository,
) : CashWidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val refreshes = MutableStateFlow(0)

    private var started = false

    override fun start() {
        if (started) return
        started = true
        // The figures change with each cash expense, count or income: no periodic job is needed.
        scope.launch {
            combine(cashCounts.balance, available.today, refreshes) { cash, today, _ -> cash to today }
                .collect { (cash, today) -> draw(cash, today) }
        }
    }

    override fun refresh() = refreshes.update { it + 1 }

    private fun draw(cash: CashBalance?, today: Long?) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, CashWidgetProvider::class.java))
        if (ids.isEmpty()) return
        val views = RemoteViews(context.packageName, R.layout.widget_cash).apply {
            setTextViewText(
                R.id.widget_cash_amount,
                cash?.let { context.getString(R.string.widget_cash_amount, formatFcfa(it.amount)) }
                    ?: context.getString(R.string.widget_cash_none),
            )
            if (today == null) {
                setViewVisibility(R.id.widget_available, View.GONE)
            } else {
                setViewVisibility(R.id.widget_available, View.VISIBLE)
                setTextViewText(R.id.widget_available, context.getString(R.string.widget_available, formatFcfa(today)))
            }
            setOnClickPendingIntent(R.id.widget_root, open(action = null, requestCode = OPEN_REQUEST))
            setOnClickPendingIntent(
                R.id.widget_add,
                open(action = QuickEntryLaunch.ACTION_QUICK_ENTRY, requestCode = ADD_REQUEST),
            )
        }
        manager.updateAppWidget(ids, views)
    }

    // Opens the app, on the quick entry when [action] asks for it.
    private fun open(action: String?, requestCode: Int): PendingIntent {
        val intent = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Cauri has a launcher activity"
        }.setAction(action ?: Intent.ACTION_MAIN)
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {
        const val OPEN_REQUEST = 1
        const val ADD_REQUEST = 2
    }
}
