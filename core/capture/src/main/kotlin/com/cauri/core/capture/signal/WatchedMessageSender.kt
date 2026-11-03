package com.cauri.core.capture.signal

import kotlinx.serialization.Serializable

/**
 * An operator or bank as it signs its SMS or mails, as checked on a real phone.
 *
 * @property name Sender as the message app shows it, in the title of its notification (« OrangeMoney »).
 * @property status Whether it was confirmed to send operations, not only advertising.
 * @property checkedOn Where and when it was checked.
 */
@Serializable
data class WatchedMessageSender(val name: String, val status: WatchedStatus, val checkedOn: String)
