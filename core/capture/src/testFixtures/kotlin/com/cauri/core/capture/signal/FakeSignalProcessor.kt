package com.cauri.core.capture.signal

import com.cauri.core.data.model.NewAccount
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [SignalProcessor] for tests: it records the calls and reads nothing. */
class FakeSignalProcessor : SignalProcessor {
    override val proposedAccounts = MutableStateFlow<List<NewAccount>>(emptyList())
    override val ignoredAccounts = MutableStateFlow<List<NewAccount>>(emptyList())

    /** How many times [process] ran. */
    var processed = 0
    val followed = mutableListOf<String>()
    val ignored = mutableListOf<String>()

    override suspend fun process() {
        processed++
    }

    override suspend fun follow(provider: String) {
        followed += provider
        proposedAccounts.value = proposedAccounts.value.filterNot { it.provider == provider }
        ignoredAccounts.value = ignoredAccounts.value.filterNot { it.provider == provider }
    }

    override suspend fun ignore(provider: String) {
        ignored += provider
        ignoredAccounts.value += proposedAccounts.value.filter { it.provider == provider }
        proposedAccounts.value = proposedAccounts.value.filterNot { it.provider == provider }
    }
}
