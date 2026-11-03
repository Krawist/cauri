package com.cauri.core.parsing.model

import java.time.Instant

/**
 * A message as the notification gave it, to be read by the templates.
 *
 * @property packageName Package name of the app that posted the notification.
 * @property title Title of the notification: the sender of an SMS or a mail; `null` when it has none.
 * @property text Text of the notification.
 * @property receivedAt When the notification was posted.
 */
data class ReceivedMessage(val packageName: String, val title: String?, val text: String, val receivedAt: Instant)
