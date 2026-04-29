package io.github.mdsadiqueinam.hidayah.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShieldCard(
    headline: String,
    subHeadline: String,
    imagePath: String?,
    modifier: Modifier = Modifier,
    appName: String = "App Name",
    usage: String = "0m",
    attempts: Int = 0,
    editable: Boolean = true,
    onHeadlineChange: (String) -> Unit = {},
    onSubHeadlineChange: (String) -> Unit = {},
    onImageClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
) {
    val cardBackground = MaterialTheme.colorScheme.inverseSurface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(48.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        ShieldContent(
            headline = headline,
            subHeadline = subHeadline,
            imagePath = imagePath,
            modifier = Modifier.padding(24.dp),
            appName = appName,
            usage = usage,
            attempts = attempts,
            editable = editable,
            onHeadlineChange = onHeadlineChange,
            onSubHeadlineChange = onSubHeadlineChange,
            onImageClick = onImageClick,
            onCloseClick = onCloseClick,
            onOpenClick = onOpenClick
        )
    }
}
