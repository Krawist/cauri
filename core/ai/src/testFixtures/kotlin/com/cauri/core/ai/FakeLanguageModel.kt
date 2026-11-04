package com.cauri.core.ai

/** [LanguageModel] for tests: the test sets whether it is available and what it answers; the prompts are kept. */
class FakeLanguageModel : LanguageModel {
    var available = true

    var answer: String? = "[]"

    val prompts = mutableListOf<String>()

    override suspend fun isAvailable() = available

    override suspend fun answer(prompt: String): String? {
        prompts += prompt
        return answer
    }
}
