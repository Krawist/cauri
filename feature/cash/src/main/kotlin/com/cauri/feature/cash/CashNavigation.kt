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

/**
 * Split of the money missing after a count (CAU-16).
 *
 * @property counted Amount counted, in FCFA.
 * @property expected Estimated cash before the count, in FCFA.
 */
@Serializable
internal data class GapSplitRoute(val counted: Long, val expected: Long)

fun NavController.navigateToCashCount() = navigate(CashGraph)

/** Cash count (CAU-15) and split of the gap (CAU-16). [onBack] leaves the cash screens. */
fun NavGraphBuilder.cashGraph(navController: NavController, onBack: () -> Unit) {
    navigation<CashGraph>(startDestination = CashCountRoute) {
        composable<CashCountRoute> {
            CashCountScreen(
                onBack = onBack,
                onSplitGap = { counted, expected -> navController.navigate(GapSplitRoute(counted, expected)) },
            )
        }
        composable<GapSplitRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.cash_split_title),
                subtitle = stringResource(R.string.cash_coming_soon),
                actionLabel = stringResource(R.string.cash_back),
                onAction = { navController.popBackStack() },
            )
        }
    }
}
