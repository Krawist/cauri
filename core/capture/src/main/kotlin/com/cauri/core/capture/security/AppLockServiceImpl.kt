package com.cauri.core.capture.security

import android.app.Activity
import com.cauri.core.data.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** Default implementation for [AppLockService]. */
@Singleton
internal class AppLockServiceImpl @Inject constructor(
    private val security: DeviceSecurity,
    private val preferences: UserPreferencesRepository,
) : AppLockService {
    override val enabled: Flow<Boolean> = preferences.appLock

    override fun available() = security.available()

    override suspend fun set(on: Boolean, activity: Activity): Boolean {
        val recognised = security.authenticate(activity)
        if (recognised) preferences.setAppLock(on)
        return recognised
    }
}
