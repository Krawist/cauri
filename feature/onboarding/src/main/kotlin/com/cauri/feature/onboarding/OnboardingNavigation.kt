package com.cauri.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.feature.onboarding.accounts.AccountsScreen
import com.cauri.feature.onboarding.categories.CategoriesScreen
import com.cauri.feature.onboarding.habits.HabitsScreen
import com.cauri.feature.onboarding.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingGraph

@Serializable
internal data object ProfileRoute

@Serializable
internal data object HabitsRoute

@Serializable
internal data object CategoriesRoute

@Serializable
internal data object AccountsRoute

/**
 * Profile, payment habits, categories, accounts and permissions (CAU-06 to CAU-11). Each step hands its answers to the
 * [OnboardingViewModel] of the graph, which saves them all after the last one. Until the next steps exist, the
 * onboarding ends after the accounts.
 */
fun NavGraphBuilder.onboardingGraph(navController: NavController, onCompleted: () -> Unit) {
    navigation<OnboardingGraph>(startDestination = ProfileRoute) {
        composable<ProfileRoute> { entry ->
            val onboarding = onboardingViewModel(navController, entry)
            ProfileScreen(
                onProfileAnswered = { answers ->
                    onboarding.onProfileAnswered(answers)
                    navController.navigate(HabitsRoute)
                },
            )
        }
        composable<HabitsRoute> { entry ->
            val onboarding = onboardingViewModel(navController, entry)
            HabitsScreen(
                onHabitsAnswered = { answers ->
                    onboarding.onHabitsAnswered(answers)
                    navController.navigate(CategoriesRoute)
                },
            )
        }
        composable<CategoriesRoute> { entry ->
            val onboarding = onboardingViewModel(navController, entry)
            CategoriesScreen(
                onCategoriesChosen = { categories ->
                    onboarding.onCategoriesChosen(categories)
                    navController.navigate(AccountsRoute)
                },
            )
        }
        composable<AccountsRoute> { entry ->
            val onboarding = onboardingViewModel(navController, entry)
            val completed by onboarding.completed.collectAsStateWithLifecycle()
            LaunchedEffect(completed) {
                if (completed) onCompleted()
            }
            AccountsScreen(onAccountsChosen = onboarding::complete)
        }
    }
}

// The view model of the whole graph, shared by its steps.
@Composable
private fun onboardingViewModel(navController: NavController, entry: NavBackStackEntry): OnboardingViewModel {
    val graphEntry = remember(entry) { navController.getBackStackEntry<OnboardingGraph>() }
    return hiltViewModel(graphEntry)
}
