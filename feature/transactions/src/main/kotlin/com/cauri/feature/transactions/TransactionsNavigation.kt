package com.cauri.feature.transactions

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.feature.transactions.activity.ActivityScreen
import kotlinx.serialization.Serializable

/** Activité tab. */
@Serializable
data object ActivityGraph

@Serializable
internal data object ActivityRoute

/**
 * « Activité » (CAU-30): a line opens the edit sheet through [onEditTransaction]. Quick entry and the edit sheet are
 * sheets, [QuickEntrySheet] and [EditTransactionSheet], not destinations.
 */
fun NavGraphBuilder.activityGraph(onEditTransaction: (Long) -> Unit) {
    navigation<ActivityGraph>(startDestination = ActivityRoute) {
        composable<ActivityRoute> { ActivityScreen(onEditTransaction = onEditTransaction) }
    }
}
