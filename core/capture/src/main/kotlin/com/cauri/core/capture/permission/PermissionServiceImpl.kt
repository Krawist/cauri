package com.cauri.core.capture.permission

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cauri.core.capture.CauriNotificationListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [PermissionService]. */
@Singleton
internal class PermissionServiceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    PermissionService {
    override fun status() = PermissionStatus(
        notificationAccess = context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context),
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled(),
        location = when {
            !granted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                !granted(Manifest.permission.ACCESS_COARSE_LOCATION) -> LocationAccess.DENIED
            granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> LocationAccess.ALWAYS
            else -> LocationAccess.WHILE_IN_USE
        },
    )

    // From Android 11, the screen of Cauri's listener alone; some phones lack it, so the list of listeners otherwise.
    override fun notificationAccessSettings(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val detail = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
                Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                ComponentName(context, CauriNotificationListener::class.java).flattenToString(),
            )
            if (detail.resolveActivity(context.packageManager) != null) return detail
        }
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    override fun notificationSettings(): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    override fun appSettings(): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
