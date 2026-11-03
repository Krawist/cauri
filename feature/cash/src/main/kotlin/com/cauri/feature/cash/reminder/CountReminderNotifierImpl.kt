package com.cauri.feature.cash.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cauri.core.data.model.CountReminderReason
import com.cauri.core.design.R as DesignR
import com.cauri.feature.cash.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [CountReminderNotifier]. */
@Singleton
internal class CountReminderNotifierImpl @Inject constructor(@ApplicationContext private val context: Context) :
    CountReminderNotifier {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun show(reason: CountReminderReason): Boolean {
        val manager = NotificationManagerCompat.from(context)
        val allowed = manager.areNotificationsEnabled() &&
            (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                )
        if (!allowed) return false
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.cash_reminder_channel))
                .setDescription(context.getString(R.string.cash_reminder_channel_description))
                .build(),
        )
        val text = context.getString(
            when (reason) {
                CountReminderReason.WITHDRAWAL -> R.string.cash_reminder_withdrawal
                CountReminderReason.EXPENSES -> R.string.cash_reminder_expenses
                CountReminderReason.DAYS_WITHOUT_COUNT -> R.string.cash_reminder_days
                CountReminderReason.EVENING -> R.string.cash_reminder_evening
            },
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(DesignR.drawable.logo_coin_silhouette)
            .setColor(ContextCompat.getColor(context, DesignR.color.notification_accent))
            .setContentTitle(context.getString(R.string.cash_reminder_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openCount())
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
        return true
    }

    // Opens the app on the cash count, whatever screen it was on.
    private fun openCount(): PendingIntent {
        val intent = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Cauri has a launcher activity"
        }.setAction(CountReminders.ACTION_COUNT_CASH)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {
        const val CHANNEL_ID = "cash_count_reminders"
        const val NOTIFICATION_ID = 1_001
    }
}
