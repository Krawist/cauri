package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.capture.battery.BatteryService
import com.cauri.core.capture.health.CaptureHealthService
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.data.repository.RawSignalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Whether the home screen warns that Cauri receives no more messages, read again each time the screen comes back:
 * only while a permission the capture needs is missing, as the notification (with every permission given, a silence is
 * the user's own rhythm).
 */
@HiltViewModel
internal class CaptureHealthViewModel @Inject constructor(
    private val rawSignalRepository: RawSignalRepository,
    private val captureHealthService: CaptureHealthService,
    private val permissionService: PermissionService,
    private val batteryService: BatteryService,
    private val clock: Clock,
) : ViewModel() {
    private val state = MutableStateFlow(false)

    /** Whether no message of the watched senders arrived for a month while a permission is missing. */
    val paused: StateFlow<Boolean> = state.asStateFlow()

    /** Reads the permissions and the last message again, such as back from Réglages. */
    fun refresh() {
        viewModelScope.launch {
            val blocked = !permissionService.status().notificationAccess || !batteryService.unrestricted()
            state.value = blocked &&
                captureHealthService.silent(lastSignal = rawSignalRepository.lastReceivedAt(), now = clock.instant())
        }
    }
}
