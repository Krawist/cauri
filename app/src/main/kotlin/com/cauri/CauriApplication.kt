package com.cauri

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.cauri.core.capture.health.CaptureHealth
import com.cauri.core.capture.place.PlaceWatcher
import com.cauri.core.capture.reminder.EveningSummary
import com.cauri.core.design.glass.supportsGlassBlur
import com.cauri.feature.cash.reminder.CountReminders
import com.cauri.feature.places.discovery.PlaceDiscovery
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
internal class CauriApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var placeWatcher: PlaceWatcher

    /** Whether glass surfaces blur, chosen once per process, never per frame or per screen. */
    val glassBlur: Boolean by lazy { supportsGlassBlur() }

    // Workers get their dependencies from Hilt; WorkManager starts on demand with this configuration.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        CountReminders.schedule(this)
        CaptureHealth.schedule(this)
        placeWatcher.start()
        PlaceDiscovery.schedule(this)
        EveningSummary.schedule(this)
    }
}
