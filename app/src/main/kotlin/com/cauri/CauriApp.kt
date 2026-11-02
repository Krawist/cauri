package com.cauri

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.cash.cashGraph
import com.cauri.feature.cash.navigateToCashCount
import com.cauri.feature.home.HomeGraph
import com.cauri.feature.home.homeGraph
import com.cauri.feature.onboarding.OnboardingGraph
import com.cauri.feature.onboarding.onboardingGraph
import com.cauri.feature.transactions.navigateToQuickEntry
import com.cauri.feature.transactions.transactionsGraph

/** Root of the app: the onboarding on first launch, then the home screen. */
@Composable
internal fun CauriApp(uiState: MainUiState, onOnboardingCompleted: () -> Unit) {
    when (uiState) {
        // Plain background while the preferences load (a few milliseconds).
        MainUiState.Loading -> Box(Modifier.fillMaxSize().background(CauriTheme.palette.background))
        is MainUiState.Ready -> CauriNavHost(
            onboardingCompleted = uiState.onboardingCompleted,
            onOnboardingCompleted = onOnboardingCompleted,
        )
    }
}

@Composable
private fun CauriNavHost(
    onboardingCompleted: Boolean,
    onOnboardingCompleted: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    // Chosen once: a new start destination would rebuild the graph under the back stack when the onboarding completes.
    val startDestination = remember { if (onboardingCompleted) HomeGraph else OnboardingGraph }
    NavHost(navController = navController, startDestination = startDestination) {
        onboardingGraph(
            onCompleted = {
                onOnboardingCompleted()
                navController.navigate(HomeGraph) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
        )
        homeGraph(
            onCountCash = { navController.navigateToCashCount() },
            onAddExpense = { navController.navigateToQuickEntry() },
        )
        cashGraph(onBack = { navController.popBackStack() })
        transactionsGraph(onBack = { navController.popBackStack() })
    }
}
