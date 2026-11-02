package com.cauri.feature.transactions

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

/** Activité tab. */
@Serializable
data object ActivityGraph

@Serializable
internal data object ActivityRoute

/** Activity (CAU-30). Quick entry is a sheet, [QuickEntrySheet], not a destination. */
fun NavGraphBuilder.activityGraph() {
    navigation<ActivityGraph>(startDestination = ActivityRoute) {
        composable<ActivityRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.transactions_activity_title),
                subtitle = stringResource(R.string.transactions_coming_soon),
            )
        }
    }
}
