package com.cauri.feature.cash

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
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
            GapSplitScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack<CashGraph>(inclusive = true) },
            )
        }
    }
}
