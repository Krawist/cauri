package com.cauri.core.capture.signal

import kotlinx.serialization.Serializable

/**
 * An app whose notifications Cauri keeps, as checked on a real phone.
 *
 * @property packageName Package name of the app, as Android reports it, never guessed.
 * @property name Name of the app, for people reading the list.
 * @property status Whether its notifications of transactions were seen.
 * @property checkedOn Where and when it was checked.
 */
@Serializable
data class WatchedApp(val packageName: String, val name: String, val status: WatchedStatus, val checkedOn: String)
