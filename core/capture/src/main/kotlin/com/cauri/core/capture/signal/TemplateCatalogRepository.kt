package com.cauri.core.capture.signal

import com.cauri.core.parsing.model.TemplateCatalog

/** The message templates Cauri reads the signals with. */
interface TemplateCatalogRepository {
    /** The catalogue shipped with the app (`message_templates.json` of `:core:parsing`). */
    val catalog: TemplateCatalog
}
