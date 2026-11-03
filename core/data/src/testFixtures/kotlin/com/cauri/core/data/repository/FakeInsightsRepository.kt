package com.cauri.core.data.repository

import com.cauri.core.data.model.Insights

/** [InsightsRepository] for tests: it gives the figures the test sets. */
class FakeInsightsRepository : InsightsRepository {
    var figures =
        Insights(spent = 0, cash = 0, unidentified = 0, categories = emptyList(), overrun = null, habit = null)

    override suspend fun insights() = figures
}
