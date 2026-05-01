package io.github.mdsadiqueinam.hidayah.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.mdsadiqueinam.hidayah.data.ShieldImage
import io.github.mdsadiqueinam.hidayah.data.defaultShieldImageResources

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShieldSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShieldSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ShieldSettingsScreenContent(
        onBack = onBack,
        uiState = uiState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShieldSettingsScreenContent(
    onBack: () -> Unit,
    uiState: ShieldSettingsUiState,
    modifier: Modifier = Modifier
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { uiState.onImageSelected(ShieldImage.UriImage(it.toString())) }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { uiState.onVideoSelected(it.toString()) }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { uiState.onAudioSelected(it.toString()) }
    }

    val defaultShieldImages = remember {
        defaultShieldImageResources.map { ShieldImage.Resource(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ShieldSettingsTopBar(onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .then(modifier),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item { ShieldSettingsHeader() }
            item {
                ShieldContentEditor(
                    uiState = uiState,
                    imagePickerLauncher = imagePickerLauncher,
                    videoPickerLauncher = videoPickerLauncher,
                    audioPickerLauncher = audioPickerLauncher,
                    defaultShieldImages = defaultShieldImages
                )
            }
            item { ShieldLivePreview(uiState) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShieldSettingsTopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Hidayah",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    )
}

@Composable
private fun ShieldSettingsHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "PRIVACY CONFIGURATION",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            "Shield Settings",
            style = MaterialTheme.typography.displayMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            "Customize your sanctuary. Define how the Global Shield appears when you're reclaiming your focus.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ShieldContentEditor(
    uiState: ShieldSettingsUiState,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    videoPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    audioPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    defaultShieldImages: List<ShieldImage>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        ShieldContentCard(uiState)
        ShieldBackgroundSelectorCard(
            uiState = uiState,
            imagePickerLauncher = imagePickerLauncher,
            videoPickerLauncher = videoPickerLauncher,
            audioPickerLauncher = audioPickerLauncher,
            defaultShieldImages = defaultShieldImages
        )
        ShieldActionButtons(uiState)
    }
}

@Composable
private fun ShieldContentCard(
    uiState: ShieldSettingsUiState,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        icon = Icons.Default.EditNote,
        title = "Content"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            InputField(
                label = "Headline",
                value = uiState.config.headline,
                onValueChange = uiState.onHeadlineChange,
                placeholder = "Enter shield headline..."
            )
            InputField(
                label = "Sub-headline",
                value = uiState.config.subHeadline,
                onValueChange = uiState.onSubHeadlineChange,
                placeholder = "Enter shield message...",
                singleLine = false,
                minLines = 2
            )
        }
    }
}

@Composable
private fun ShieldBackgroundSelectorCard(
    uiState: ShieldSettingsUiState,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    videoPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    audioPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    defaultShieldImages: List<ShieldImage>,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        icon = Icons.Default.Image,
        title = "Shield Background"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            ModeSegmentedControl(
                useVideo = uiState.config.useVideo,
                onUseVideoToggle = uiState.onUseVideoToggle
            )

            if (uiState.config.useVideo) {
                MediaPickerCard(
                    title = "Background Video",
                    icon = Icons.Default.VideoLibrary,
                    path = uiState.config.videoPath,
                    onPick = { videoPickerLauncher.launch("video/*") },
                    onClear = { uiState.onVideoSelected(null) }
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text(
                        text = "Static Image",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ShieldImageSelectorCardContent(
                        uiState,
                        imagePickerLauncher,
                        defaultShieldImages
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ambient Audio",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MediaPickerCard(
                        title = "Background Audio",
                        icon = Icons.Default.MusicNote,
                        path = uiState.config.audioPath,
                        onPick = { audioPickerLauncher.launch("audio/*") },
                        onClear = { uiState.onAudioSelected(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShieldImageSelectorCardContent(
    uiState: ShieldSettingsUiState,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    defaultShieldImages: List<ShieldImage>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload", style = MaterialTheme.typography.labelLarge)
            }
        }
        ShieldImageSelector(uiState, defaultShieldImages)
    }
}
