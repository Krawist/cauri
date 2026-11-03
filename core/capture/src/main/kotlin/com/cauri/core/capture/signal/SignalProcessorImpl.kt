package com.cauri.core.capture.signal

import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.model.RawSignal
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.AccountRepository
import com.cauri.core.data.repository.RawSignalRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.parsing.MessageParser
import com.cauri.core.parsing.model.ReceivedMessage
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Default implementation for [SignalProcessor]. */
@Singleton
@Suppress("LongParameterList") // One dependency per kind of data the processing reads or writes.
internal class SignalProcessorImpl @Inject constructor(
    private val rawSignalRepository: RawSignalRepository,
    private val templateCatalogRepository: TemplateCatalogRepository,
    private val messageParser: MessageParser,
    private val detectionService: DetectionService,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : SignalProcessor {
    // The listener and the screens may ask at the same time: a signal must be read only once.
    private val mutex = Mutex()

    override val proposedAccounts: Flow<List<NewAccount>> = preferencesRepository.proposedAccounts.map(::known)

    override val ignoredAccounts: Flow<List<NewAccount>> = preferencesRepository.ignoredAccounts.map(::known)

    override suspend fun process() {
        mutex.withLock {
            rawSignalRepository.unparsed().forEach { signal ->
                messageParser.parse(
                    message = signal.toMessage(),
                    catalog = templateCatalogRepository.catalog,
                    zone = ZoneId.systemDefault(),
                )?.let { parsed ->
                        val account = accountRepository.accountOf(parsed.provider)
                        if (account != null) {
                            transactionRepository.recordDetected(
                                detectionService.detect(signal = signal, parsed = parsed, account = account),
                            )
                        } else if (knownAccount(parsed.provider) != null) {
                            preferencesRepository.proposeAccount(parsed.provider)
                        }
                    }
            }
        }
    }

    override suspend fun follow(provider: String) {
        val account = knownAccount(provider)
        if (account != null && accountRepository.accountOf(provider) == null) accountRepository.createAccount(account)
        preferencesRepository.clearAccountChoice(provider)
        process()
    }

    override suspend fun ignore(provider: String) {
        preferencesRepository.ignoreAccount(provider)
    }

    private fun known(providers: Set<String>): List<NewAccount> = providers.sorted().mapNotNull(::knownAccount)

    private fun RawSignal.toMessage() =
        ReceivedMessage(packageName = sender, title = title, text = text, receivedAt = receivedAt)
}
