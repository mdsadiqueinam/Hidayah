package io.github.mdsadiqueinam.hidayah

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.mdsadiqueinam.hidayah.ui.navigation.NavGraph
import io.github.mdsadiqueinam.hidayah.ui.navigation.Screen

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    RecheckOverlayPermissionOnResume(
        onPermissionCheck = { hasOverlayPermission = Settings.canDrawOverlays(context) }
    )

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

    MainScaffold(
        navController = navController,
        modifier = modifier
    )
}

@Composable
private fun RecheckOverlayPermissionOnResume(onPermissionCheck: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestPermissionCheck = rememberUpdatedState(onPermissionCheck)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            latestPermissionCheck.value()
        }
    }
}

@Composable
private fun MainScaffold(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(Screen.Home, Screen.Report, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = shouldShowBottomBar(currentDestination, items)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    items = items,
                    currentDestination = currentDestination,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()).then(modifier)) {
            NavGraph(navController = navController)
        }
    }
}

private fun shouldShowBottomBar(
    currentDestination: NavDestination?,
    items: List<Screen>
): Boolean {
    return currentDestination?.route in items.map { it.route }
}

@Composable
private fun MainBottomBar(
    items: List<Screen>,
    currentDestination: NavDestination?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier.fillMaxWidth().then(modifier),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(80.dp)
        ) {
            items.forEach { screen ->
                MainNavigationBarItem(
                    screen = screen,
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = { onItemClick(screen.route) }
                )
            }
        }
    }
}

@Composable
private fun MainNavigationBarItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = when (screen) {
                    Screen.Home -> Icons.Default.Home
                    Screen.Report -> Icons.Default.Info
                    Screen.Settings -> Icons.Default.Settings
                    else -> Icons.Default.Settings
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        label = {
            Text(
                text = screen.route.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            unselectedIconColor = MaterialTheme.colorScheme.outline,
            unselectedTextColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun PermissionDialog(onGoToSettings: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Permission Required") },
        text = {
            Text(
                "Hidayah needs the 'Display over other apps' permission to show " +
                    "the protection shield and keep you focused. Please enable it in settings."
            )
        },
        confirmButton = {
            Button(onClick = onGoToSettings) {
                Text("Go to Settings")
            }
        }
    )
}
