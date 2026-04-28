package io.github.mdsadiqueinam.hidayah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShieldSettingsScreen(
    onBack: () -> Unit,
    viewModel: ShieldSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Shield") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CustomizeShieldCard(
                headline = uiState.config.headline,
                subHeadline = uiState.config.subHeadline,
                onHeadlineChange = uiState.onHeadlineChange,
                onSubHeadlineChange = uiState.onSubHeadlineChange
            )

            OutlinedButton(
                onClick = uiState.onUseDefault,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = borderStroke()
            ) {
                Text(
                    "Use Default Shield",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))

@Composable
fun CustomizeShieldCard(
    headline: String,
    subHeadline: String,
    onHeadlineChange: (String) -> Unit,
    onSubHeadlineChange: (String) -> Unit
) {
    val cardBackground = MaterialTheme.colorScheme.inverseSurface
    val inputBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    val accentColor = MaterialTheme.colorScheme.tertiary
    val onCardColor = MaterialTheme.colorScheme.inverseOnSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(48.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle placeholder for image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(onCardColor.copy(alpha = 0.1f))
                    .clickable { /* Add image */ },
                contentAlignment = Alignment.Center
            ) {
                // Empty for now as per image
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tap on circle to add image",
                color = onCardColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Headline Input Box (Stylized)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = inputBackground
            ) {
                BasicTextField(
                    value = headline,
                    onValueChange = onHeadlineChange,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = onCardColor,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(20.dp),
                    cursorColor = onCardColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-headline Input Box (Stylized)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = inputBackground
            ) {
                BasicTextField(
                    value = subHeadline,
                    onValueChange = onSubHeadlineChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = onCardColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(20.dp),
                    cursorColor = onCardColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "App Name",
                style = MaterialTheme.typography.titleLarge,
                color = onCardColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Today's Usage: 42m",
                style = MaterialTheme.typography.bodyMedium,
                color = onCardColor.copy(alpha = 0.8f)
            )

            Text(
                text = "Open Attempts: 3",
                style = MaterialTheme.typography.bodyMedium,
                color = onCardColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Button(
                onClick = { /* Close */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* Open */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) {
                Text("Open", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    cursorColor: Color = Color.Unspecified
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        modifier = modifier,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(cursorColor)
    )
}
