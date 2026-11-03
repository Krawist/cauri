package com.cauri.core.capture.place

/** [PlaceWatcher] for tests: it counts the calls. */
class FakePlaceWatcher : PlaceWatcher {
    var starts = 0
    var refreshes = 0

    override fun start() {
        starts++
    }

    override fun refresh() {
        refreshes++
    }
}
