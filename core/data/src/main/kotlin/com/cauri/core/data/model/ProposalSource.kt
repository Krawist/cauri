package com.cauri.core.data.model

/** What a proposal of Cauri was made from, to measure how often each source is right. */
enum class ProposalSource {
    /** The place where the transaction happened. */
    PLACE,

    /** The merchant or person the message names. */
    MERCHANT,

    /** The template that read the message. */
    TEMPLATE,
}
