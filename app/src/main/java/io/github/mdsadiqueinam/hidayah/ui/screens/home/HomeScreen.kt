package io.github.mdsadiqueinam.hidayah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private const val GRADIENT_RADIUS = 800f
private const val GRADIENT_CENTER_X = 1000f

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onNavigateToAppSettings: (String) -> Unit = {},
    viewModel: HomeViewModel? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }
    ObserveHomeResume(viewModel = viewModel)

    Scaffold(
        modifier = modifier,
        topBar = { HomeTopAppBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Insights, contentDescription = "Insights")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        HomeScreenContent(
            uiState = uiState,
            innerPadding = innerPadding,
            onNavigateToAppSettings = onNavigateToAppSettings,
            onAddClick = { showAddDialog = true }
        )

        if (showAddDialog) {
            AppSelectionDialog(onDismiss = { showAddDialog = false })
        }
    }
}

@Composable
private fun ObserveHomeResume(viewModel: HomeViewModel?) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel?.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    innerPadding: PaddingValues,
    onNavigateToAppSettings: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = androidx.compose.ui.geometry.Offset(GRADIENT_CENTER_X, 0f),
                    radius = GRADIENT_RADIUS
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            if (!uiState.isUsageStatsPermissionGranted) {
                item { PermissionRequiredCard() }
            }
            item { StatsHeroCard(uiState) }
            item { ProtectionSection(uiState) }
            item { TopAppsSection(uiState) }
            item {
                ControlledAppsSection(
                    uiState = uiState,
                    onAddClick = onAddClick,
                    onAppClick = onNavigateToAppSettings
                )
            }
            item { MindfulQuoteCard() }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val mockUiState = HomeUiState(
        totalScreenTime = "3h 15m",
        controlledScreenTime = "1h 20m",
        totalScreenTimeMs =
            java.util.concurrent.TimeUnit.HOURS.toMillis(3) +
                java.util.concurrent.TimeUnit.MINUTES.toMillis(15),
        screenTimeLimit = "5h 0m",
        screenTimeLimitMs = java.util.concurrent.TimeUnit.HOURS.toMillis(5),
        screenTimeStatus = "Excellent",
        isProtectionActive = true,
        selectedPauseDuration = null,
        topApps = listOf(
            TopApp("Instagram", "1h 12m", "Social", "com.instagram.android"),
            TopApp("YouTube", "54m", "Entertainment", "com.google.android.youtube"),
            TopApp("TikTok", "48m", "Short Video", "com.zhiliaoapp.musically")
        ),
        controlledApps = listOf(
            ControlledAppWithUsage(
                io.github.mdsadiqueinam.hidayah.data.ControlledApp("com.facebook.katana", "Facebook"),
                "22m",
                "20m"
            ),
            ControlledAppWithUsage(
                io.github.mdsadiqueinam.hidayah.data.ControlledApp("com.whatsapp", "WhatsApp"),
                "45m",
                "15m"
            )
        )
    )

    io.github.mdsadiqueinam.hidayah.ui.theme.HidayahTheme {
        HomeScreen(uiState = mockUiState)
    }
}
