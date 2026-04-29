package io.github.mdsadiqueinam.hidayah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.mdsadiqueinam.hidayah.ui.navigation.NavGraph
import io.github.mdsadiqueinam.hidayah.ui.navigation.Screen
import io.github.mdsadiqueinam.hidayah.ui.theme.HidayahTheme
import io.github.mdsadiqueinam.hidayah.service.AppTrackerService
import android.content.Intent

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Start the background tracking service
        startService(Intent(this, AppTrackerService::class.java))
        
        setContent {
            HidayahTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Report,
        Screen.Settings,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on detail screens
    val showBottomBar = currentDestination?.route in items.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = when (screen) {
                                        Screen.Home -> Icons.Default.Home
                                        Screen.Report -> Icons.Default.Info
                                        Screen.Settings -> Icons.Default.Settings
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = null
                                )
                            },
                            label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // We only apply the bottom padding from the Scaffold's innerPadding to avoid double padding at the top.
        // The individual screens will handle their own top/status bar padding via their own Scaffolds or insets.
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            NavGraph(navController = navController)
        }
    }
}