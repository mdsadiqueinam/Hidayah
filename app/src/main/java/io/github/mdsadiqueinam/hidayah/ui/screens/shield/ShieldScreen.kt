package io.github.mdsadiqueinam.hidayah.ui.screens.shield

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import io.github.mdsadiqueinam.hidayah.data.ShieldImage
import io.github.mdsadiqueinam.hidayah.data.defaultShieldImageResources

private const val BACKGROUND_IMAGE_ALPHA = 0.6f
private const val BACKGROUND_OVERLAY_ALPHA = 0.4f
private const val PROGRESS_BAR_WIDTH_MOCK = 0.3f

@Composable
fun ShieldScreen(
    onClose: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShieldViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shieldImage = rememberShieldImage(uiState.shieldConfig.imagePath)
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(
        uiState.shieldConfig.useVideo,
        uiState.shieldConfig.videoPath,
        uiState.shieldConfig.audioPath
    ) {
        if (uiState.shieldConfig.useVideo && uiState.shieldConfig.videoPath != null) {
            exoPlayer.setMediaItem(MediaItem.fromUri(uiState.shieldConfig.videoPath!!))
            exoPlayer.prepare()
        } else if (!uiState.shieldConfig.useVideo && uiState.shieldConfig.audioPath != null) {
            exoPlayer.setMediaItem(MediaItem.fromUri(uiState.shieldConfig.audioPath!!))
            exoPlayer.prepare()
        } else {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(modifier)
    ) {
        if (uiState.shieldConfig.useVideo && uiState.shieldConfig.videoPath != null) {
            ShieldVideoBackground(exoPlayer)
        } else {
            ShieldBackgroundImage(shieldImage)
        }
        ShieldGradientOverlay()
        ShieldContentColumn(uiState, onClose, onOpen)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ShieldVideoBackground(exoPlayer: ExoPlayer, modifier: Modifier = Modifier) {
    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
            .fillMaxSize()
            .alpha(BACKGROUND_IMAGE_ALPHA)
            .drawWithContent {
                drawContent()
                drawRect(Color.Black.copy(alpha = BACKGROUND_OVERLAY_ALPHA))
            }
    )
}

@Composable
private fun rememberShieldImage(imagePath: String?): ShieldImage {
    return remember(imagePath) {
        when {
            imagePath.isNullOrBlank() -> ShieldImage.Resource(defaultShieldImageResources.first())
            imagePath.startsWith("res:") -> {
                val resId = imagePath.substringAfter("res:").toIntOrNull()
                if (resId != null) ShieldImage.Resource(resId)
                else ShieldImage.Resource(defaultShieldImageResources.first())
            }

            else -> ShieldImage.UriImage(imagePath)
        }
    }
}

@Composable
private fun ShieldBackgroundImage(shieldImage: ShieldImage, modifier: Modifier = Modifier) {
    AsyncImage(
        model = when (shieldImage) {
            is ShieldImage.Resource -> shieldImage.resId
            is ShieldImage.UriImage -> shieldImage.uri
        },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxSize()
            .alpha(BACKGROUND_IMAGE_ALPHA)
            .drawWithContent {
                drawContent()
                drawRect(Color.Black.copy(alpha = BACKGROUND_OVERLAY_ALPHA))
            }
    )
}

@Composable
private fun ShieldGradientOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
}

@Composable
private fun ShieldContentColumn(
    uiState: ShieldUiState,
    onClose: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ShieldIconSection()
        Spacer(modifier = Modifier.height(40.dp))
        ShieldTextSection(uiState)
        Spacer(modifier = Modifier.height(48.dp))
        ShieldUsageStats(uiState)
        Spacer(modifier = Modifier.weight(1f))
        ShieldActionButtons(onClose, onOpen)
        Spacer(modifier = Modifier.height(16.dp))
        ShieldProgressIndicator()
    }
}

@Composable
private fun ShieldIconSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(100.dp)
            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .background(Color.White.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
private fun ShieldTextSection(
    uiState: ShieldUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = uiState.shieldConfig.headline.ifEmpty { "Peace of Mind" },
            style = MaterialTheme.typography.displayMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = uiState.shieldConfig.subHeadline.ifEmpty {
                "Your sanctuary is active. Take a deep breath and reconnect with the present moment."
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ShieldUsageStats(
    uiState: ShieldUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${uiState.controlledApp?.appName ?: "App"}: ${uiState.usageTime}",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ShieldActionButtons(
    onClose: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                "Close Application",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text(
                "Continue Anyway",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ShieldProgressIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(PROGRESS_BAR_WIDTH_MOCK)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "PROTECTION ACTIVE",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
