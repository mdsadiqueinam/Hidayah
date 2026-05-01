package io.github.mdsadiqueinam.hidayah.ui.screens.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeTopAppBar(modifier: Modifier = Modifier) {
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
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Hidayah",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

@Composable
fun PermissionRequiredCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Usage Access Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hidayah needs usage access to track your screen time and provide insights.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun StatsHeroCard(uiState: HomeUiState, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            StatsHeroHeader(screenTimeStatus = uiState.screenTimeStatus)
            Spacer(modifier = Modifier.height(16.dp))
            StatsTimeSummary(uiState = uiState)
            Spacer(modifier = Modifier.height(24.dp))
            StatsProgress(uiState = uiState)
        }
    }
}

@Composable
private fun StatsHeroHeader(screenTimeStatus: String, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TODAY'S FOCUS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 1.2.sp
        )
        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape) {
            Text(
                text = screenTimeStatus,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun StatsTimeSummary(uiState: HomeUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Total Screen Time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = uiState.totalScreenTime,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Controlled Time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = uiState.controlledScreenTime,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun StatsProgress(uiState: HomeUiState, modifier: Modifier = Modifier) {
    val progress = if (uiState.screenTimeLimitMs > 0) {
        (uiState.totalScreenTimeMs.toFloat() / uiState.screenTimeLimitMs.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val remainingPercentage = ((1f - progress) * 100).toInt()

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Used ${uiState.totalScreenTime} of ${uiState.screenTimeLimit} limit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$remainingPercentage% Remaining",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProtectionSection(uiState: HomeUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ProtectionHeader(isActive = uiState.isProtectionActive)
        Spacer(modifier = Modifier.height(20.dp))
        ProtectionCard(
            isActive = uiState.isProtectionActive,
            onProtectionToggle = uiState.onProtectionToggle,
            selectedPauseDuration = uiState.selectedPauseDuration,
            onPauseDurationChange = uiState.onPauseDurationChange,
            pausedUntilMs = uiState.pausedUntilMs
        )
    }
}

@Composable
private fun ProtectionHeader(isActive: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Shield",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = if (isActive) "Active" else "Paused",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun ProtectionCard(
    isActive: Boolean,
    onProtectionToggle: (Boolean) -> Unit,
    selectedPauseDuration: String?,
    onPauseDurationChange: (String?) -> Unit,
    pausedUntilMs: Long,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(pausedUntilMs) {
        while (pausedUntilMs > System.currentTimeMillis()) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remainingTime = remember(pausedUntilMs, currentTime) {
        if (pausedUntilMs > currentTime) {
            val diff = pausedUntilMs - currentTime
            val h = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diff)
            val m = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val s = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
            else String.format("%02d:%02d", m, s)
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            ProtectionCardHeader(
                isActive = isActive,
                onProtectionToggle = onProtectionToggle
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PAUSE PROTECTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )
                if (remainingTime != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $remainingTime REMAINING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.9f),
                        letterSpacing = 1.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PauseOptionsRow(
                options = listOf("5m", "15m", "1h"),
                selected = selectedPauseDuration,
                onSelect = {
                    val next = if (selectedPauseDuration == it) null else it
                    onPauseDurationChange(next)
                }
            )
        }
    }
}

@Composable
private fun ProtectionCardHeader(
    isActive: Boolean,
    onProtectionToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryFixed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Global Protection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Active for all selected apps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.8f)
                )
            }
        }

        Switch(
            checked = isActive,
            onCheckedChange = onProtectionToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryFixed,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
private fun PauseOptionsRow(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(option) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

