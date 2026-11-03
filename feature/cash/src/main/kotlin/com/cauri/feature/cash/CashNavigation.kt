package com.cauri.feature.cash

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data object CashGraph

@Serializable
internal data object CashCountRoute

/**
 * Split of the money missing after a count (CAU-16), or after the balance typed for a digital account (CAU-52).
 *
 * @property counted Amount counted, or balance typed, in FCFA.
 * @property expected Estimated cash before the count, or balance shown before the one typed, in FCFA.
 * @property accountId Digital account whose balance was typed; `null` for the cash.
 */
@Serializable
internal data class GapSplitRoute(val counted: Long, val expected: Long, val accountId: Long? = null)

fun NavController.navigateToCashCount() = navigate(CashGraph)

/** Split of the gap between the [typed] balance of [accountId] and the [expected] one shown before, in FCFA. */
fun NavController.navigateToBalanceGap(accountId: Long, typed: Long, expected: Long) =
    navigate(GapSplitRoute(counted = typed, expected = expected, accountId = accountId))

/** Cash count (CAU-15) and split of the gap (CAU-16). [onBack] leaves the cash screens. */
fun NavGraphBuilder.cashGraph(navController: NavController, onBack: () -> Unit) {
    navigation<CashGraph>(startDestination = CashCountRoute) {
        composable<CashCountRoute> {
            CashCountScreen(
                onBack = onBack,
                onSplitGap = { counted, expected -> navController.navigate(GapSplitRoute(counted, expected)) },
            )
        }
        composable<GapSplitRoute> { entry ->
            val digital = entry.toRoute<GapSplitRoute>().accountId != null
            GapSplitScreen(
                onBack = { navController.popBackStack() },
                // A digital account's split was opened from Accueil, not from the cash count.
                onSaved = {
                    if (digital) {
                        navController.popBackStack()
                    } else {
                        navController.popBackStack<CashGraph>(
                            inclusive = true,
                        )
                    }
                },
            )
        }
    }
}
