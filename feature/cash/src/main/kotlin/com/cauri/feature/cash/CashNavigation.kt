package com.cauri.feature.cash

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.core.design.component.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
data object CashGraph

@Serializable
internal data object CashCountRoute

fun NavController.navigateToCashCount() = navigate(CashGraph)

/** Cash count and split of the gap (CAU-15, CAU-16). */
fun NavGraphBuilder.cashGraph(onBack: () -> Unit) {
    navigation<CashGraph>(startDestination = CashCountRoute) {
        composable<CashCountRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.cash_count_title),
                subtitle = stringResource(R.string.cash_coming_soon),
                actionLabel = stringResource(R.string.cash_back),
                onAction = onBack,
            )
        }
    }
}
