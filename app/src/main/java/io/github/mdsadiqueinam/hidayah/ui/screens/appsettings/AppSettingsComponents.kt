package io.github.mdsadiqueinam.hidayah.ui.screens.appsettings

import android.widget.ImageView
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.mdsadiqueinam.hidayah.data.ControlledApp

@Composable
fun AppIdentityHero(app: ControlledApp, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.2f)
            )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                AppIcon(packageName = app.packageName, modifier = Modifier.fillMaxSize())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = app.appName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Managing your digital sanctuary",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RestrictionSettingsCard(
    dailyLimit: Int,
    sessionDuration: Int,
    onDailyLimitChange: (Int?) -> Unit,
    onSessionDurationChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            RestrictionSettingsHeader()
            Spacer(modifier = Modifier.height(24.dp))
            DailyLimitSection(dailyLimit, onDailyLimitChange)
            Spacer(modifier = Modifier.height(32.dp))
            SessionDurationSection(sessionDuration, onSessionDurationChange)
        }
    }
}

@Composable
private fun RestrictionSettingsHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "RESTRICTION SETTINGS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun DailyLimitSection(
    dailyLimit: Int,
    onDailyLimitChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Daily Time Limit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            LimitValueDisplay(dailyLimit)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = if (dailyLimit == 0) 0f else dailyLimit.toFloat(),
            onValueChange = { onDailyLimitChange(it.toInt()) },
            valueRange = 0f..120f,
            steps = 23,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )

        Text(
            text = "Maximum usage allowed per 24 hours.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SessionDurationSection(
    sessionDuration: Int,
    onSessionDurationChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Session Duration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            LimitValueDisplay(sessionDuration)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = if (sessionDuration == 0) 0f else sessionDuration.toFloat(),
            onValueChange = { onSessionDurationChange(it.toInt()) },
            valueRange = 0f..30f,
            steps = 29,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )

        Text(
            text = "Break reminder after continuous usage.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LimitValueDisplay(value: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = if (value == 0) "None" else value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (value > 0) {
            Text(
                text = "m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun GentleInsightCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = Modifier.fillMaxWidth().then(modifier),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Gentle Insight",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reducing your daily limit by just 5 minutes can save you 30 hours of time every year.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ManagementSection(onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Button(
            onClick = onRemove,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Un-monitor this app",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This will stop Hidayah from tracking or limiting your usage immediately.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val icon = remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            null
        } catch (e: SecurityException) {
            null
        }
    }

    if (icon != null) {
        AndroidView(
            factory = { ctx ->
                ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setImageDrawable(icon)
                }
            },
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
