package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.capture.signal.SignalProcessor
import com.cauri.core.data.model.NewAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The account Cauri offers to follow on the home screen, having recognised its messages. Opening the home screen also
 * reads the signals left unread, such as those kept before an update of the templates.
 */
@HiltViewModel
internal class AccountProposalViewModel @Inject constructor(private val processor: SignalProcessor) : ViewModel() {
    /** The first account offered; `null` when there is none. */
    val proposal: StateFlow<NewAccount?> = processor.proposedAccounts
        .map { it.firstOrNull() }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(STOP_DELAY), initialValue = null)

    init {
        viewModelScope.launch { processor.process() }
    }

    /** Follows the account of [provider] when [follow], or stops offering it. */
    fun onChoice(provider: String, follow: Boolean) {
        viewModelScope.launch { if (follow) processor.follow(provider) else processor.ignore(provider) }
    }

    private companion object {
        // Keeps the proposal through a rotation.
        const val STOP_DELAY = 5_000L
    }
}
