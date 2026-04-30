package io.github.mdsadiqueinam.hidayah.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Report : Screen("report")
    object Settings : Screen("settings")
    object ShieldSettings : Screen("shield_settings")
    object AppSettings : Screen("app_settings/{packageName}") {
        fun createRoute(packageName: String) = "app_settings/$packageName"
    }
}
