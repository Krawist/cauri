package com.cauri.core.capture.health

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
import com.cauri.core.capture.R
import com.cauri.core.design.R as DesignR
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [CaptureHealthNotifier]. */
@Singleton
internal class CaptureHealthNotifierImpl @Inject constructor(@ApplicationContext private val context: Context) :
    CaptureHealthNotifier {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun show(): Boolean {
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
                .setName(context.getString(R.string.capture_health_channel))
                .setDescription(context.getString(R.string.capture_health_channel_description))
                .build(),
        )
        val text = context.getString(R.string.capture_health_text)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(DesignR.drawable.logo_coin_silhouette)
            .setColor(ContextCompat.getColor(context, DesignR.color.notification_accent))
            .setContentTitle(context.getString(R.string.capture_health_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openSettings())
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
        return true
    }

    // Opens the app on Réglages, whatever screen it was on.
    private fun openSettings(): PendingIntent {
        val intent = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Cauri has a launcher activity"
        }.setAction(CaptureHealth.ACTION_OPEN_SETTINGS)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {
        const val CHANNEL_ID = "capture_health"
        const val NOTIFICATION_ID = 1_002
    }
}
