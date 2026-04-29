package io.github.mdsadiqueinam.hidayah.ui.components

import android.graphics.ImageDecoder
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun ShieldContent(
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
    val context = LocalContext.current
    val onColor = if (editable) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onBackground
    val inputBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    val accentColor = MaterialTheme.colorScheme.tertiary

    val bitmap = remember(imagePath) {
        if (imagePath != null) {
            try {
                val uri = imagePath.toUri()
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circle placeholder for image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(onColor.copy(alpha = 0.1f))
                .then(
                    if (editable) {
                        Modifier.clickable { onImageClick() }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (editable) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap on circle to add image",
                color = onColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline Input Box (Stylized)
        if (editable) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = inputBackground
            ) {
                BasicTextFieldWrapper(
                    value = headline,
                    onValueChange = onHeadlineChange,
                    placeholder = "Pause. Think. Decide.",
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = onColor,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(20.dp),
                    cursorColor = onColor
                )
            }
        } else {
            Text(
                text = headline.ifEmpty { "Pause. Think. Decide." },
                style = MaterialTheme.typography.headlineSmall,
                color = onColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sub-headline Input Box (Stylized)
        if (editable) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = inputBackground
            ) {
                BasicTextFieldWrapper(
                    value = subHeadline,
                    onValueChange = onSubHeadlineChange,
                    placeholder = "Take a breath before opening this app.",
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = onColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(20.dp),
                    cursorColor = onColor
                )
            }
        } else {
            Text(
                text = subHeadline.ifEmpty { "Take a breath before opening this app." },
                style = MaterialTheme.typography.bodyMedium,
                color = onColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = appName,
            style = MaterialTheme.typography.titleLarge,
            color = onColor,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Today's Usage: $usage",
            style = MaterialTheme.typography.bodyMedium,
            color = onColor.copy(alpha = 0.8f)
        )

        Text(
            text = "Open Attempts: $attempts",
            style = MaterialTheme.typography.bodyMedium,
            color = onColor.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Button(
            onClick = onCloseClick,
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
            onClick = onOpenClick,
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

@Composable
fun BasicTextFieldWrapper(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    cursorColor: Color = Color.Unspecified
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = textStyle.copy(color = textStyle.color.copy(alpha = 0.4f)),
                textAlign = TextAlign.Center
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            modifier = Modifier.fillMaxWidth(),
            cursorBrush = SolidColor(cursorColor)
        )
    }
}
