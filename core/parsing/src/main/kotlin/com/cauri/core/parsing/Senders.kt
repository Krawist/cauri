package com.cauri.core.parsing

import com.cauri.core.parsing.model.MessageTemplate
import com.cauri.core.parsing.model.ReceivedMessage

/** Whether [message] comes from a sender of this template: its app, or its SMS or mail name, case ignored. */
internal fun MessageTemplate.isSentBy(message: ReceivedMessage): Boolean {
    val title = message.title?.trim()
    return senders.any { sender -> sender == message.packageName || sender.trim().equals(title, ignoreCase = true) }
}
