package com.cauri.core.capture.place

/** [LocationService] for tests: the phone is at [position]. */
class FakeLocationService : LocationService {
    /** Where the phone is; `null` when it cannot be located. */
    var position: Position? = null

    override suspend fun currentPosition(precise: Boolean) = position
}
