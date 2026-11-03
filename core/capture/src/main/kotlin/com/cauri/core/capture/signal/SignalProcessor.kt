package com.cauri.core.capture.signal

import com.cauri.core.data.model.NewAccount
import kotlinx.coroutines.flow.Flow

/**
 * Reads the raw signals with the templates and records what they report as transactions « à confirmer ». A message
 * about an account the user does not have creates nothing: Cauri offers to follow that account, and respects a refusal
 * until the user changes their mind.
 */
interface SignalProcessor {
    /** The accounts Cauri offers to follow, having recognised their messages. */
    val proposedAccounts: Flow<List<NewAccount>>

    /** The accounts the user chose not to follow. */
    val ignoredAccounts: Flow<List<NewAccount>>

    /**
     * Reads every signal no template read yet, oldest first, then links the debits and credits that are transfers
     * between the user's accounts; safe to call at any time, runs once at a time.
     */
    suspend fun process()

    /** Creates the account of [provider] and reads its messages received so far. */
    suspend fun follow(provider: String)

    /** Stops offering [provider]: its messages stay raw. */
    suspend fun ignore(provider: String)
}
