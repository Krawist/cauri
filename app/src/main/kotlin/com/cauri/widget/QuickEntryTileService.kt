package com.cauri.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Build
import android.service.quicksettings.TileService

/** The quick settings tile « Ajouter une dépense »: a tap opens the app on the quick entry. */
internal class QuickEntryTileService : TileService() {
    // Before Android 14 the tile can only open an activity with an intent, deprecated since.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        val intent = checkNotNull(packageManager.getLaunchIntentForPackage(packageName)) {
            "Cauri has a launcher activity"
        }.setAction(QuickEntryLaunch.ACTION_QUICK_ENTRY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
