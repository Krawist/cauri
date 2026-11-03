package com.cauri.core.parsing

import com.cauri.core.parsing.model.TemplateCatalog

/** Reads and checks a catalogue of message templates. */
interface TemplateCatalogReader {
    /**
     * Reads the catalogue written in [json]. Fails with an [IllegalArgumentException] when a template cannot be used:
     * an invalid pattern, no `amount` group, a `date` group without date format or the reverse, an unknown currency, no
     * sender, or an identifier used twice.
     */
    fun read(json: String): TemplateCatalog
}
