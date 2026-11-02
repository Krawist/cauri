package com.cauri.feature.transactions

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object TransactionsGraph

@Serializable
internal data object QuickEntryRoute

fun NavController.navigateToQuickEntry() = navigate(TransactionsGraph)

/** Quick entry, edit sheet and activity (CAU-26, CAU-28, CAU-30). */
fun NavGraphBuilder.transactionsGraph(onBack: () -> Unit) {
    navigation<TransactionsGraph>(startDestination = QuickEntryRoute) {
        composable<QuickEntryRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.transactions_quick_entry_title),
                subtitle = stringResource(R.string.transactions_coming_soon),
                actionLabel = stringResource(R.string.transactions_back),
                onAction = onBack,
            )
        }
    }
}
