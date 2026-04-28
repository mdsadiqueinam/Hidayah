package io.github.mdsadiqueinam.hidayah.ui.screens.appsettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import io.github.mdsadiqueinam.hidayah.ui.components.FlowButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    viewModel: AppSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val app = uiState.app

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (app == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { AppIdentityCard(app) }
                
                item {
                    SettingsGridCard(
                        title = "Daily Limits",
                        description = "How many times this app can be opened in a day",
                        icon = Icons.Default.Schedule,
                        options = listOf(0, 1, 2, 3, 4, 5, 6),
                        labels = listOf("No Limit", "1 time", "2 times", "3 times", "4 times", "5 times", "6 times"),
                        selectedOption = app.dailyLimit,
                        onOptionSelected = uiState.onDailyLimitChange
                    )
                }

                item {
                    SettingsGridCard(
                        title = "Open Delay",
                        description = "Wait time before app opens",
                        icon = Icons.Default.HourglassEmpty,
                        options = listOf(0, 10, 20, 30, 40, 50, 60),
                        labels = listOf("0s", "10s", "20s", "30s", "40s", "50s", "1m"),
                        selectedOption = app.openDelay,
                        onOptionSelected = uiState.onOpenDelayChange
                    )
                }

                item {
                    SettingsGridCard(
                        title = "Session Limit",
                        description = "Maximum duration for a single session",
                        icon = Icons.Default.Timer,
                        options = listOf(0, 1, 2, 5, 10, 15, 30, 45, 60, 120, 180),
                        labels = listOf("No Limit", "1m", "2m", "5m", "10m", "15m", "30m", "45m", "1h", "2h", "3h"),
                        selectedOption = app.sessionLimit,
                        onOptionSelected = uiState.onSessionLimitChange
                    )
                }

                item {
                    HardLockCard(
                        isLocked = app.isHardLocked,
                        onToggle = uiState.onHardLockToggle
                    )
                }

                item {
                    RemoveAppCard(
                        onRemove = {
                            uiState.onRemoveApp()
                            onBack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RemoveAppCard(onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onRemove
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "Remove App",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AppIdentityCard(app: ControlledApp) {
    val context = LocalContext.current
    val icon = remember(app.packageName) {
        try {
            context.packageManager.getApplicationIcon(app.packageName).toBitmap().asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {}
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(app.appName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun <T> SettingsGridCard(
    title: String,
    description: String,
    icon: ImageVector,
    options: List<T>,
    labels: List<String>,
    selectedOption: T,
    onOptionSelected: (T?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(16.dp))
            
            FlowButton(
                options = options,
                labels = labels,
                selectedOption = selectedOption,
                onOptionSelected = onOptionSelected,
                maxItemsInEachRow = 3,
                isToggleable = true
            )
        }
    }
}

@Composable
fun HardLockCard(isLocked: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Hard Lock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Completely block this app", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = isLocked, onCheckedChange = onToggle)
        }
    }
}
