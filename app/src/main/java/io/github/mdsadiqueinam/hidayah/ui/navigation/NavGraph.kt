package io.github.mdsadiqueinam.hidayah.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.mdsadiqueinam.hidayah.ui.screens.AppSettingsScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.ReportScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.SettingsScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.home.HomeScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.home.HomeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Report : Screen("report")
    object Settings : Screen("settings")
    object AppSettings : Screen("app_settings/{packageName}") {
        fun createRoute(packageName: String) = "app_settings/$packageName"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            HomeScreen(
                uiState = uiState,
                onNavigateToAppSettings = { packageName ->
                    navController.navigate(Screen.AppSettings.createRoute(packageName))
                }
            )
        }
        composable(Screen.Report.route) {
            ReportScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = Screen.AppSettings.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            AppSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
