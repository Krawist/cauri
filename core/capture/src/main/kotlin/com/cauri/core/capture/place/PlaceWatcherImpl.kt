package com.cauri.core.capture.place

import com.cauri.core.data.repository.PlaceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Default implementation for [PlaceWatcher]. */
@Singleton
internal class PlaceWatcherImpl(
    private val places: PlaceRepository,
    private val geofences: GeofenceService,
    dispatcher: CoroutineDispatcher,
) : PlaceWatcher {
    @Inject
    constructor(places: PlaceRepository, geofences: GeofenceService) : this(places, geofences, Dispatchers.Default)

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val refreshes = MutableStateFlow(0)

    private var started = false

    override fun start() {
        if (started) return
        started = true
        scope.launch {
            combine(places.watched.distinctUntilChanged(), refreshes) { watched, _ -> watched }
                .collect { geofences.watch(it) }
        }
    }

    override fun refresh() = refreshes.update { it + 1 }
}
