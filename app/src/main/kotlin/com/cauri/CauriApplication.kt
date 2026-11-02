package com.cauri

import android.app.Application
import com.cauri.core.design.glass.supportsGlassBlur
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
internal class CauriApplication : Application() {
    /** Whether glass surfaces blur, chosen once per process, never per frame or per screen. */
    val glassBlur: Boolean by lazy { supportsGlassBlur() }
}
