package io.github.mdsadiqueinam.hidayah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import io.github.mdsadiqueinam.hidayah.data.ShieldImage

private const val SHIELD_ASPECT_RATIO = 9f / 16f
private const val PROGRESS_BAR_WIDTH_FRACTION = 0.66f

@Composable
fun ShieldPreviewBox(
    uiState: ShieldSettingsUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(uiState.config.useVideo, uiState.config.videoPath, uiState.config.audioPath) {
        val config = uiState.config
        if (config.useVideo && config.videoPath != null) {
            exoPlayer.setMediaItem(MediaItem.fromUri(config.videoPath))
            exoPlayer.prepare()
        } else if (!config.useVideo && config.audioPath != null) {
            exoPlayer.setMediaItem(MediaItem.fromUri(config.audioPath))
            exoPlayer.prepare()
        } else {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(SHIELD_ASPECT_RATIO)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black)
            .border(
                8.dp,
                MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(32.dp)
            )
    ) {
        ShieldPreviewBackground(uiState, exoPlayer)
        ShieldPreviewOverlay()
        ShieldPreviewContent(uiState)
    }
}

@Composable
private fun ShieldPreviewBackground(
    uiState: ShieldSettingsUiState,
    exoPlayer: ExoPlayer
) {
    if (uiState.config.useVideo && uiState.config.videoPath != null) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        AsyncImage(
            model = when (val image = uiState.selectedImage) {
                is ShieldImage.Resource -> image.resId
                is ShieldImage.UriImage -> image.uri
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(Color.Black.copy(alpha = 0.4f))
                }
        )
    }
}

@Composable
private fun ShieldPreviewOverlay() {
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
        Box(modifier = Modifier.weight(1f))
        ShieldPreviewProgress()
    }
}

@Composable
private fun ShieldPreviewIcon() {
    Box(
        modifier = Modifier
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
private fun ShieldPreviewText(uiState: ShieldSettingsUiState) {
    Column {
        Text(
            text = uiState.config.headline.ifEmpty { "Peace of Mind" },
            style = MaterialTheme.typography.displaySmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
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
private fun ShieldPreviewProgress() {
    Column(
        modifier = Modifier
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
