package com.cauri.feature.goals

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

/** Objectifs tab. */
@Serializable
data object GoalsGraph

@Serializable
internal data object GoalsRoute

/** « Objectifs » (CAU-44): the savings, the tontines and the envelopes, and what was put aside this month. */
fun NavGraphBuilder.goalsGraph() {
    navigation<GoalsGraph>(startDestination = GoalsRoute) {
        composable<GoalsRoute> { GoalsScreen() }
    }
}
