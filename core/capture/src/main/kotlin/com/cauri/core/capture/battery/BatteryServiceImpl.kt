package com.cauri.core.capture.battery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [BatteryService]. */
@Singleton
internal class BatteryServiceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    BatteryService {
    override fun unrestricted() =
        context.getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(context.packageName) == true

    override fun appSettings(): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
}
