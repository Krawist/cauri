package com.cauri.core.capture.signal

import com.cauri.core.parsing.TemplateCatalogReader
import com.cauri.core.parsing.model.TemplateCatalog
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [TemplateCatalogRepository]: reads the bundled catalogue once. */
@Singleton
internal class TemplateCatalogRepositoryImpl @Inject constructor(private val reader: TemplateCatalogReader) :
    TemplateCatalogRepository {
    override val catalog: TemplateCatalog by lazy {
        val json = checkNotNull(javaClass.getResource(FILE)) { "$FILE is not bundled" }.readText()
        reader.read(json)
    }

    private companion object {
        const val FILE = "/message_templates.json"
    }
}
