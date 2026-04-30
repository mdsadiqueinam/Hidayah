package io.github.mdsadiqueinam.hidayah.ui.screens.appsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    AppSettingsScreenContent(uiState, onBack, modifier = modifier)
}

@Composable
fun AppSettingsTopAppBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .then(modifier),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

@Composable
fun AppSettingsScreenContent(
    uiState: AppSettingsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = uiState.app

    Scaffold(
        topBar = { AppSettingsTopAppBar(onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (app == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .then(modifier),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                item { AppIdentityHero(app) }

                item {
                    RestrictionSettingsCard(
                        dailyLimit = app.dailyLimit,
                        sessionDuration = app.sessionLimit,
                        onDailyLimitChange = uiState.onDailyLimitChange,
                        onSessionDurationChange = uiState.onSessionLimitChange
                    )
                }

                item { GentleInsightCard() }

                item {
                    ManagementSection(
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AppSettingsScreenPreview() {
    val mockApp = io.github.mdsadiqueinam.hidayah.data.ControlledApp(
        packageName = "com.instagram.android",
        appName = "Instagram",
        dailyLimit = 30,
        sessionLimit = 10
    )
    val mockUiState = AppSettingsUiState(
        app = mockApp
    )

    io.github.mdsadiqueinam.hidayah.ui.theme.HidayahTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppSettingsScreenContent(mockUiState, onBack = {})
        }
    }
}
