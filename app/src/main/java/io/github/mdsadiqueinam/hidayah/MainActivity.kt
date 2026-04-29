package io.github.mdsadiqueinam.hidayah

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.mdsadiqueinam.hidayah.service.AppTrackerService
import io.github.mdsadiqueinam.hidayah.ui.navigation.NavGraph
import io.github.mdsadiqueinam.hidayah.ui.navigation.Screen
import io.github.mdsadiqueinam.hidayah.ui.theme.HidayahTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start the background tracking service
        android.util.Log.i("MainActivity", "Starting AppTrackerService as foreground")
        ContextCompat.startForegroundService(this, Intent(this, AppTrackerService::class.java))

        setContent {
            HidayahTheme {
                MainScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var hasOverlayPermission by remember { 
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    // Re-check permission when user returns to the app
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            hasOverlayPermission = Settings.canDrawOverlays(context)
        }
    }

    if (!hasOverlayPermission) {
        PermissionDialog(
            onGoToSettings = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                )
                context.startActivity(intent)
            }
        )
    }

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

@Composable
fun PermissionDialog(onGoToSettings: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Persistent dialog, no dismiss */ },
        title = { Text("Permission Required") },
        text = { 
            Text("Hidayah needs the 'Display over other apps' permission to show the protection shield and keep you focused. Please enable it in settings.") 
        },
        confirmButton = {
            Button(onClick = onGoToSettings) {
                Text("Go to Settings")
            }
        }
    )
}