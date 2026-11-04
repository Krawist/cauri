package com.cauri.core.data.repository

import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [AvailableRepository] for tests: the test sets what is available today. */
class FakeAvailableRepository : AvailableRepository {
    override val today = MutableStateFlow<Long?>(null)
}
