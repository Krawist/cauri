package com.cauri.core.capture.reminder

import com.cauri.core.data.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Default implementation for [ReminderNotificationSweeper]: one place for every way a card is answered (a card of
 * Accueil, the edit sheet, « Ce soir », « Tout valider »), so that none leaves its notification behind.
 */
@Singleton
internal class ReminderNotificationSweeperImpl(
    private val transactions: TransactionRepository,
    private val notifier: ReminderNotifier,
    dispatcher: CoroutineDispatcher,
) : ReminderNotificationSweeper {
    @Inject
    constructor(transactions: TransactionRepository, notifier: ReminderNotifier) :
        this(transactions, notifier, Dispatchers.Default)

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private var started = false

    override fun start() {
        if (started) return
        started = true
        scope.launch {
            transactions.pending
                .map { cards -> cards.map { it.transactionId }.toSet() }
                .distinctUntilChanged()
                .collect(notifier::keepOnly)
        }
    }
}
