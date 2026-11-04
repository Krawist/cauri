package com.cauri.core.capture.signal

import com.cauri.core.ai.LanguageModel
import com.cauri.core.capture.reminder.ReminderDecider
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.model.RawSignal
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.AccountRepository
import com.cauri.core.data.repository.RawSignalRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.data.repository.TransferRepository
import com.cauri.core.data.repository.VisitRepository
import com.cauri.core.parsing.MessageParser
import com.cauri.core.parsing.ModelMessageReader
import com.cauri.core.parsing.model.ParsedMessage
import com.cauri.core.parsing.model.ReceivedMessage
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val transferRepository: TransferRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val visitRepository: VisitRepository,
    private val reminders: ReminderDecider,
    private val languageModel: LanguageModel,
    private val modelReader: ModelMessageReader,
    private val clock: Clock,
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
                )?.let { record(signal, it) }
            }
            // Then the phone's model, when it has one, for the messages no template knows.
            if (languageModel.isAvailable()) readWithModel()
            // A transfer between two accounts comes as two messages: once both are read, they become one transfer.
            transferRepository.linkTransfers()
            // A payment read during a visit to a shop is what was spent there.
            visitRepository.attachTransactions(clock.instant())
            // Each new card: notify now, in the evening, or not at all.
            reminders.decidePending()
        }
    }

    // The operation [parsed] in [signal], on its account; an account the user does not have is offered instead.
    private suspend fun record(signal: RawSignal, parsed: ParsedMessage) {
        val account = accountRepository.accountOf(parsed.provider)
        if (account != null) {
            val operation = detectionService.detect(
                signal = signal,
                parsed = parsed,
                account = account,
                accounts = accountRepository.accounts.first(),
            )
            transactionRepository.recordDetected(operation)
        } else if (knownAccount(parsed.provider) != null) {
            preferencesRepository.proposeAccount(parsed.provider)
        }
    }

    // Each recent message no template read is asked about once, a few at a time: the model takes seconds each. Only
    // what it copied from the message is kept (ModelMessageReader); the operation waits « à confirmer » as any other.
    private suspend fun readWithModel() {
        rawSignalRepository.unreadByModel(since = clock.instant() - MODEL_WINDOW, limit = MODEL_BATCH)
            .forEach { signal ->
                val message = signal.toMessage()
                // No answer, such as AICore busy: the message is asked about again next time.
                val answer = languageModel.answer(modelReader.prompt(message)) ?: return@forEach
                rawSignalRepository.markReadByModel(signal.id)
                modelReader.read(answer, message, templateCatalogRepository.catalog)?.let { record(signal, it) }
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

    private companion object {
        // An older message is history: what it reports was spent long ago.
        val MODEL_WINDOW: Duration = Duration.ofDays(7)
        const val MODEL_BATCH = 10
    }
}
