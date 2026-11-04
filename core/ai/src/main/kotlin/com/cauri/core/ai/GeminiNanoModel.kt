package com.cauri.core.ai

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/** [LanguageModel] on Gemini Nano, with ML Kit's Prompt API; unavailable on the phones AICore does not serve. */
@Singleton
internal class GeminiNanoModel @Inject constructor() : LanguageModel {
    private val model by lazy { Generation.getClient() }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var downloading = false

    override suspend fun isAvailable(): Boolean = when (status()) {
        FeatureStatus.AVAILABLE -> true
        FeatureStatus.DOWNLOADABLE -> {
            download()
            false
        }
        else -> false
    }

    override suspend fun answer(prompt: String): String? = try {
        model.generateContent(
            // The same answer for the same text: nothing to invent.
            generateContentRequest(TextPart(prompt)) { temperature = 0f },
        ).candidates.firstOrNull()?.text
    } catch (_: GenAiException) {
        null
    }

    // AICore fails on a phone it does not serve: the model is then unavailable.
    private suspend fun status(): Int = try {
        model.checkStatus()
    } catch (_: GenAiException) {
        FeatureStatus.UNAVAILABLE
    }

    // Once per process: the model comes over the network, by AICore, on Wi-Fi or data as the phone decides.
    private fun download() {
        if (downloading) return
        downloading = true
        scope.launch {
            model.download()
                .catch { downloading = false }
                .collect { if (it is DownloadStatus.DownloadFailed) downloading = false }
        }
    }
}
