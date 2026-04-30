package io.github.mdsadiqueinam.hidayah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.mdsadiqueinam.hidayah.data.ShieldImage

private const val SHIELD_ASPECT_RATIO = 9f / 16f
private const val PROGRESS_BAR_WIDTH_FRACTION = 0.66f

@Composable
fun SectionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                trailing()
            }
            Spacer(modifier = Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
            ),
            singleLine = singleLine,
            minLines = minLines
        )
    }
}

@Composable
fun ShieldImageSelector(
    uiState: ShieldSettingsUiState,
    defaultShieldImages: List<ShieldImage>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        defaultShieldImages.forEach { image ->
            val isSelected = uiState.selectedImage == image
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isSelected) 4.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { uiState.onImageSelected(image) }
            ) {
                AsyncImage(
                    model = when (image) {
                        is ShieldImage.Resource -> image.resId
                        is ShieldImage.UriImage -> image.uri
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShieldActionButtons(
    uiState: ShieldSettingsUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = uiState.onResetToDefaults,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset to Defaults", style = MaterialTheme.typography.labelLarge)
        }
        Button(
            onClick = uiState.onSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text("Save Changes", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ShieldLivePreview(
    uiState: ShieldSettingsUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "LIVE PREVIEW",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer))
                Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer))
                Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
            }
        }

        ShieldPreviewBox(uiState)

        Text(
            "This is how your shield will appear on your device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )
    }
}

@Composable
fun ShieldPreviewBox(
    uiState: ShieldSettingsUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(SHIELD_ASPECT_RATIO)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black)
            .border(8.dp, MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(32.dp))
    ) {
        // Shield Background
        AsyncImage(
            model = when (val image = uiState.selectedImage) {
                is ShieldImage.Resource -> image.resId
                is ShieldImage.UriImage -> image.uri
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().drawWithContent {
                drawContent()
                drawRect(Color.Black.copy(alpha = 0.4f))
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Shield Content
        ShieldPreviewContent(uiState)
    }
}

@Composable
fun ShieldPreviewContent(
    uiState: ShieldSettingsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ShieldPreviewIcon()
        Spacer(modifier = Modifier.height(32.dp))
        ShieldPreviewText(uiState)
        Box(modifier = Modifier.fillMaxHeight().weight(1f))
        ShieldPreviewProgress()
    }
}

@Composable
fun ShieldPreviewIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp)
            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .background(Color.White.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun ShieldPreviewText(
    uiState: ShieldSettingsUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = uiState.config.headline.ifEmpty { "Peace of Mind" },
            style = MaterialTheme.typography.displaySmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = uiState.config.subHeadline.ifEmpty {
                "Your sanctuary is active. Take a deep breath and reconnect with the present moment."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ShieldPreviewProgress(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(PROGRESS_BAR_WIDTH_FRACTION)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "12 MINUTES REMAINING",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
