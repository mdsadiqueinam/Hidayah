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
import io.github.mdsadiqueinam.hidayah.ui.screens.appsettings.AppSettingsScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.home.HomeScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.home.HomeViewModel
import io.github.mdsadiqueinam.hidayah.ui.screens.report.ReportScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.settings.SettingsScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.settings.ShieldSettingsScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.shield.ShieldScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Report : Screen("report")
    object Settings : Screen("settings")
    object ShieldSettings : Screen("shield_settings")
    object AppSettings : Screen("app_settings/{packageName}") {
        fun createRoute(packageName: String) = "app_settings/$packageName"
    }
    object Shield : Screen("shield/{packageName}") {
        fun createRoute(packageName: String) = "shield/$packageName"
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
                },
                viewModel = viewModel
            )
        }
        composable(Screen.Report.route) {
            ReportScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToShieldSettings = {
                    navController.navigate(Screen.ShieldSettings.route)
                }
            )
        }
        composable(Screen.ShieldSettings.route) {
            ShieldSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.AppSettings.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            AppSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.Shield.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            ShieldScreen(
                onClose = { navController.popBackStack() },
                onOpen = { navController.popBackStack() }
            )
        }
    }
}
