package com.cauri.core.ai

/**
 * The language model of the phone, when it has one (Gemini Nano through AICore). It may interpret free text or word a
 * message; it never computes a number.
 */
interface LanguageModel {
    /** Whether the model can answer now; on a phone that can get it, its download starts and it answers later. */
    suspend fun isAvailable(): Boolean

    /** The model's answer to [prompt]; `null` when it cannot answer. */
    suspend fun answer(prompt: String): String?
}
