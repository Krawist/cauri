package com.cauri.core.data.repository

import com.cauri.core.data.model.Insights

/** The figures of « Analyses ». */
interface InsightsRepository {
    /** The insights of the current month, up to now. */
    suspend fun insights(): Insights
}
