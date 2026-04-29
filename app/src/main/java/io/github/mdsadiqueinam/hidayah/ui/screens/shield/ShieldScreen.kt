package io.github.mdsadiqueinam.hidayah.ui.screens.shield

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.mdsadiqueinam.hidayah.ui.components.ShieldCard

@Composable
fun ShieldScreen(
    onClose: () -> Unit,
    onOpen: () -> Unit,
    viewModel: ShieldViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        ShieldCard(
            headline = uiState.shieldConfig.headline,
            subHeadline = uiState.shieldConfig.subHeadline,
            imagePath = uiState.shieldConfig.imagePath,
            appName = uiState.controlledApp?.appName ?: "App Name",
            usage = uiState.usageTime,
            attempts = uiState.attempts,
            editable = false,
            onCloseClick = onClose,
            onOpenClick = onOpen,
            modifier = Modifier.padding(16.dp)
        )
    }
}
