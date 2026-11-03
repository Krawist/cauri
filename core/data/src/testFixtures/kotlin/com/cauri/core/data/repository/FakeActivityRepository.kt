package com.cauri.core.data.repository

import com.cauri.core.data.model.ActivityLine
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [ActivityRepository] for tests: the lines are set by the test. */
class FakeActivityRepository : ActivityRepository {
    override val lines = MutableStateFlow(emptyList<ActivityLine>())
}
