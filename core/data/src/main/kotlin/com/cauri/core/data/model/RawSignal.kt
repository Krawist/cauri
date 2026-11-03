package com.cauri.core.data.model

import java.time.Instant

/**
 * A notification kept from a watched sender, as received.
 *
 * @property id Identifier.
 * @property sender Package name of the app that posted it.
 * @property title Title of the notification: the sender of an SMS or a mail; `null` when it has none.
 * @property text Text of the notification.
 * @property receivedAt When it was posted.
 */
data class RawSignal(val id: Long, val sender: String, val title: String?, val text: String, val receivedAt: Instant)
