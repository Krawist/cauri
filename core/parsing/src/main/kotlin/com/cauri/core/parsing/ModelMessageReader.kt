package com.cauri.core.parsing

import com.cauri.core.parsing.model.ParsedMessage
import com.cauri.core.parsing.model.ReceivedMessage
import com.cauri.core.parsing.model.TemplateCatalog

/**
 * Reads, with the phone's language model, a message of a known sender that no template recognises: the model only says
 * what kind of operation it is and copies the amounts as written; code keeps them only when they are written in the
 * message.
 */
interface ModelMessageReader {
    /** What the model is asked about [message]. */
    fun prompt(message: ReceivedMessage): String

    /**
     * The operation of the model's [answer] about [message]: on the account of the one provider whose templates of
     * [catalog] the sender sends, in their currency; its amount, fees and balance each written in the message. `null`
     * when the model found no operation, when the sender is not the sender of one provider, or when a value is not in
     * the message.
     */
    fun read(answer: String, message: ReceivedMessage, catalog: TemplateCatalog): ParsedMessage?

    companion object {
        /** Template identifier of the operations the model read, as a template's. */
        const val TEMPLATE_ID = "model.gemini-nano"

        /** Version of the prompt and of the checks: the operations read before a change can be told apart. */
        const val VERSION = 1
    }
}
