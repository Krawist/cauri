package com.cauri.core.parsing

import com.cauri.core.parsing.model.ParsedMessage
import com.cauri.core.parsing.model.ReceivedMessage
import com.cauri.core.parsing.model.TemplateCatalog
import java.time.ZoneId

/** Reads the operations reported by the messages of the operators and the banks, with the templates. */
interface MessageParser {
    /**
     * Reads [message] with the first template of [catalog] sent by its sender that matches it, the dates of the
     * message being in [zone]. `null` when no template recognises it, or when a value or the currency it found cannot
     * be read: the message then stays a raw signal, to be read again when a template is added or fixed.
     */
    fun parse(message: ReceivedMessage, catalog: TemplateCatalog, zone: ZoneId): ParsedMessage?
}
