package com.cauri.core.parsing

import com.cauri.core.parsing.model.MessageTemplate
import com.cauri.core.parsing.model.TemplateCatalog
import java.util.Currency
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** Default implementation for [TemplateCatalogReader]. Public so that the app's Hilt modules can bind it. */
@Singleton
class TemplateCatalogReaderImpl @Inject constructor() : TemplateCatalogReader {
    override fun read(json: String): TemplateCatalog {
        val catalog = Json.decodeFromString(TemplateCatalog.serializer(), json)
        catalog.templates.forEach(::check)
        val reused = catalog.templates.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
        require(reused.isEmpty()) { "Template ids used more than once: $reused" }
        return catalog
    }

    private fun check(template: MessageTemplate) {
        Regex(template.pattern)
        Currency.getInstance(template.currency)
        require(template.senders.isNotEmpty()) { "Template ${template.id} has no sender" }
        require("(?<amount>" in template.pattern) { "Template ${template.id} has no amount group" }
        require(("(?<date>" in template.pattern) == (template.dateFormat != null)) {
            "Template ${template.id} needs both a date group and a date format, or neither"
        }
    }
}
