package com.cauri.core.data.repository

import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.MonthSpending
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [ActivityRepository] for tests: the lines and the spending are set by the test. */
class FakeActivityRepository : ActivityRepository {
    override val lines = MutableStateFlow(emptyList<ActivityLine>())

    override val monthSpending = MutableStateFlow(MonthSpending(cash = 0, digital = 0))
}
