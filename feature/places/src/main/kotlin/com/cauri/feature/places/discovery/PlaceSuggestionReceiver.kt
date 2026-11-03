package com.cauri.feature.places.discovery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cauri.core.data.repository.DiscoveryRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** « Non » of the notification: the place is ignored for good, and the question goes. */
@AndroidEntryPoint
internal class PlaceSuggestionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var discovery: DiscoveryRepository

    @Inject
    lateinit var notifier: PlaceSuggestionNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_SUGGESTION, -1).takeIf { it > 0 } ?: return
        notifier.dismiss()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                discovery.answer(id = id, accepted = false)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        /** Id of the suggestion refused. */
        const val EXTRA_SUGGESTION = "suggestion"
    }
}
