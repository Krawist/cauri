package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.capture.health.CaptureHealthService
import com.cauri.core.data.repository.RawSignalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Whether the home screen warns that Cauri receives no more messages, read each time the screen opens. */
@HiltViewModel
internal class CaptureHealthViewModel @Inject constructor(
    rawSignalRepository: RawSignalRepository,
    captureHealthService: CaptureHealthService,
    clock: Clock,
) : ViewModel() {
    private val state = MutableStateFlow(false)

    /** Whether no message of the watched senders arrived for a month. */
    val silent: StateFlow<Boolean> = state.asStateFlow()

    init {
        viewModelScope.launch {
            state.value = captureHealthService.silent(
                lastSignal = rawSignalRepository.lastReceivedAt(),
                now = clock.instant(),
            )
        }
    }
}
