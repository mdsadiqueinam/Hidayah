package io.github.mdsadiqueinam.hidayah.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

private const val DIALOG_MAX_HEIGHT_FRACTION = 0.85f

@Composable
fun AppSelectionDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(DIALOG_MAX_HEIGHT_FRACTION)
                .then(modifier)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                AppSelectionDialogHeader()
                Spacer(modifier = Modifier.height(16.dp))
                AppSelectionSearchField(uiState)
                Spacer(modifier = Modifier.height(16.dp))
                AppSelectionList(uiState)
                Spacer(modifier = Modifier.height(16.dp))
                AppSelectionDialogActions(onDismiss, uiState)
            }
        }
    }
}

@Composable
private fun AppSelectionDialogHeader(modifier: Modifier = Modifier) {
    Text(
        text = "Add Controlled Apps",
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun AppSelectionSearchField(
    uiState: AppSelectionUiState,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = uiState.onSearchQueryChange,
        placeholder = { Text("Search by name") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
private fun AppSelectionList(
    uiState: AppSelectionUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.weight(1f)) {
        items(uiState.filteredApps, key = { it.packageName }) { app ->
            AppRow(
                appName = app.appName,
                packageName = app.packageName,
                usage = app.formattedUsage,
                isSelected = uiState.selectedPackages.contains(app.packageName),
                onToggle = { uiState.onAppToggle(app.packageName) }
            )
        }
    }
}

@Composable
private fun AppSelectionDialogActions(
    onDismiss: () -> Unit,
    uiState: AppSelectionUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text("Cancel")
        }
        TextButton(
            onClick = {
                uiState.onSave(uiState.selectedPackages)
                onDismiss()
            }
        ) {
            Text("Done")
        }
    }
}

@Composable
fun AppRow(
    appName: String,
    packageName: String,
    usage: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    val icon = remember(packageName) {
        try {
            packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            null
        } catch (e: SecurityException) {
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )
        } else {
            Surface(
                modifier = Modifier.size(42.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {}
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = usage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
    }
}
