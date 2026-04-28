package io.github.mdsadiqueinam.hidayah.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.mdsadiqueinam.hidayah.ui.screens.ReportScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.SettingsScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.home.HomeScreen
import io.github.mdsadiqueinam.hidayah.ui.screens.home.HomeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Report : Screen("report")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            HomeScreen(uiState = uiState)
        }
        composable(Screen.Report.route) {
            ReportScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
