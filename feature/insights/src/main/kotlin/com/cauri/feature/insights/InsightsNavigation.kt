package com.cauri.feature.insights

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

/** Analyses tab. */
@Serializable
data object InsightsGraph

@Serializable
internal data object InsightsRoute

/** « Analyses » (CAU-45, CAU-47): the budgets Cauri suggests from real spending. */
fun NavGraphBuilder.insightsGraph() {
    navigation<InsightsGraph>(startDestination = InsightsRoute) {
        composable<InsightsRoute> { InsightsScreen() }
    }
}
