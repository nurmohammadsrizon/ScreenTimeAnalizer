package com.srizon.screentimeguard.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.srizon.screentimeguard.ui.MainViewModel
import com.srizon.screentimeguard.ui.screens.AddAppScreen
import com.srizon.screentimeguard.ui.screens.DashboardScreen
import com.srizon.screentimeguard.ui.screens.OnboardingPermissionsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val ADD_APP = "add_app"
}

@Composable
fun AppNavGraph(
    viewModel: MainViewModel,
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 6 } },
        exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 6 } },
        popEnterTransition = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { -it / 6 } },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 6 } }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingPermissionsScreen(onAllGranted = {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onAddApp = { navController.navigate(Routes.ADD_APP) }
            )
        }
        composable(Routes.ADD_APP) {
            AddAppScreen(
                viewModel = viewModel,
                onDone = { navController.popBackStack() }
            )
        }
    }
}
