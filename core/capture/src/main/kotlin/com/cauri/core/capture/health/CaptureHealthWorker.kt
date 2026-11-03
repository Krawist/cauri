package com.cauri.core.capture.health

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cauri.core.capture.battery.BatteryService
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.RawSignalRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock
import kotlinx.coroutines.flow.first

/**
 * Checks once a day whether the capture went silent ([CaptureHealthService]) while a permission it needs is missing,
 * and warns the user, once a week at most.
 */
// Injected: the data and the permissions the rule reads, the rule, the notification and the clock.
@Suppress("LongParameterList")
@HiltWorker
internal class CaptureHealthWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val rawSignalRepository: RawSignalRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val permissionService: PermissionService,
    private val batteryService: BatteryService,
    private val captureHealthService: CaptureHealthService,
    private val notifier: CaptureHealthNotifier,
    private val clock: Clock,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val now = clock.instant()
        val warn = captureHealthService.shouldWarn(
            lastSignal = rawSignalRepository.lastReceivedAt(),
            lastWarning = preferencesRepository.lastCaptureWarning.first(),
            blocked = !permissionService.status().notificationAccess || !batteryService.unrestricted(),
            now = now,
        )
        if (warn && notifier.show()) preferencesRepository.setLastCaptureWarning(now)
        return Result.success()
    }
}
