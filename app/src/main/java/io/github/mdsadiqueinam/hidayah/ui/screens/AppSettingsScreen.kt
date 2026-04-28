package io.github.mdsadiqueinam.hidayah.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppSettingsUiState(
    val app: ControlledApp? = null,
    val onDailyLimitChange: (Int) -> Unit = {},
    val onOpenDelayChange: (Int) -> Unit = {},
    val onSessionLimitChange: (Int) -> Unit = {},
    val onHardLockToggle: (Boolean) -> Unit = {}
)

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AppRepository
) : ViewModel() {
    val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()

    init {
        loadAppData()
    }

    private fun loadAppData() {
        viewModelScope.launch {
            val app = repository.getControlledApp(packageName)
            _uiState.update { state ->
                state.copy(
                    app = app,
                    onDailyLimitChange = { limit -> updateApp { it.copy(dailyLimit = limit) } },
                    onOpenDelayChange = { delay -> updateApp { it.copy(openDelay = delay) } },
                    onSessionLimitChange = { limit -> updateApp { it.copy(sessionLimit = limit) } },
                    onHardLockToggle = { locked -> updateApp { it.copy(isHardLocked = locked) } }
                )
            }
        }
    }

    private fun updateApp(update: (ControlledApp) -> ControlledApp) {
        val currentApp = _uiState.value.app ?: return
        val updatedApp = update(currentApp)
        _uiState.update { it.copy(app = updatedApp) }
        viewModelScope.launch {
            repository.updateControlledApp(updatedApp)
        }
    }
}

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            }
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
    options: List<T>,
    labels: List<String>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            
            // Grid of buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = option == selectedOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { onOptionSelected(option) },
                        label = { Text(labels[index]) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
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
            Column(Modifier.weight(1f)) {
                Text("Hard Lock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Completely block this app", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = isLocked, onCheckedChange = onToggle)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxItemsInEachRow = maxItemsInEachRow
    ) {
        content()
    }
}
