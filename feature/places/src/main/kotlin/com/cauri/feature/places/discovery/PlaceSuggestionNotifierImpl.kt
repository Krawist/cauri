package com.cauri.feature.places.discovery

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cauri.core.data.model.PlaceSuggestion
import com.cauri.core.design.R as DesignR
import com.cauri.feature.places.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [PlaceSuggestionNotifier]. */
@Singleton
internal class PlaceSuggestionNotifierImpl @Inject constructor(@ApplicationContext private val context: Context) :
    PlaceSuggestionNotifier {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun show(suggestion: PlaceSuggestion): Boolean {
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
                .setName(context.getString(R.string.places_discovery_channel))
                .setDescription(context.getString(R.string.places_discovery_channel_description))
                .build(),
        )
        val text = suggestionText(context, suggestion)
        val add = addPlace()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(DesignR.drawable.logo_coin_silhouette)
            .setColor(ContextCompat.getColor(context, DesignR.color.notification_accent))
            .setContentTitle(context.getString(R.string.places_suggestion_title))
            .setContentText(text)
            .setContentIntent(add)
            .addAction(0, context.getString(R.string.places_suggestion_yes), add)
            .addAction(0, context.getString(R.string.places_suggestion_no), refuse(suggestion.id))
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
        return true
    }

    override fun dismiss() = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

    // Opens the app on the screen of a new place, at the place suggested.
    private fun addPlace(): PendingIntent {
        val intent = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Cauri has a launcher activity"
        }.setAction(PlaceDiscovery.ACTION_ADD_SUGGESTED_PLACE)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    // « Non », answered without opening the app.
    private fun refuse(suggestionId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(
            context,
            PlaceSuggestionReceiver::class.java,
        ).putExtra(PlaceSuggestionReceiver.EXTRA_SUGGESTION, suggestionId),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private companion object {
        const val CHANNEL_ID = "place_suggestions"
        const val NOTIFICATION_ID = 1_003
    }
}
