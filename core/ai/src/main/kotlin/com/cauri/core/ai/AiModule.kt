package com.cauri.core.ai

import com.cauri.core.parsing.freetext.FreeTextReader
import com.cauri.core.parsing.freetext.FreeTextReaderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AiModule {
    @Binds
    abstract fun bindLanguageModel(impl: GeminiNanoModel): LanguageModel

    @Binds
    abstract fun bindFreeTextReader(impl: FreeTextReaderImpl): FreeTextReader
}
