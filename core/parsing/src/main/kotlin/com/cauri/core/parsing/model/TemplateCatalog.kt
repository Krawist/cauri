package com.cauri.core.parsing.model

import kotlinx.serialization.Serializable

/**
 * The message templates, versioned data that can be updated without a new release of the app.
 *
 * @property version Version of the catalogue (« 2026.11.03 »).
 * @property templates Templates, tried in this order: the first one that matches a message reads it.
 */
@Serializable
data class TemplateCatalog(val version: String, val templates: List<MessageTemplate>)
