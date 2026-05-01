package io.github.mdsadiqueinam.hidayah.ui.screens.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mdsadiqueinam.hidayah.util.DateTimeUtils
import java.util.Calendar

private const val DONUT_STOKE_WIDTH = 40f
private const val DONUT_START_ANGLE = -90f
private val BAR_CONTAINER_HEIGHT = 120.dp
private const val FULL_PERCENTAGE = 100f
private const val FULL_CIRCLE_DEGREES = 360f
private val BAR_CORNER_RADIUS = 100.dp
private const val BAR_BACKGROUND_COLOR = 0xFFE6EDE9

@Composable
fun WeeklyBarChart(
    data: List<Long>,
    modifier: Modifier = Modifier
) {
    require(data.size == 7) { "Data must contain exactly 7 days" }

    val calendar = Calendar.getInstance()

    // Generate last 7 days labels (oldest → newest)
    val days = (6 downTo 0).map { i ->
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        DateTimeUtils.getShortWeekdayName(calendar)
    }

    val maxUsage = data.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, usage ->
            val fraction = usage.toFloat() / maxUsage.toFloat()

            BarItem(
                label = days[index],
                fraction = fraction,
                // highlight today (last item)
                isHighlight = index == data.lastIndex,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BarItem(
    label: String,
    fraction: Float,
    isHighlight: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BAR_CONTAINER_HEIGHT)
                .clip(RoundedCornerShape(topStart = BAR_CORNER_RADIUS, topEnd = BAR_CORNER_RADIUS))
                .background(Color(BAR_BACKGROUND_COLOR))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction)
                    .align(Alignment.BottomCenter)
                    .clip(
                        RoundedCornerShape(
                            topStart = BAR_CORNER_RADIUS,
                            topEnd = BAR_CORNER_RADIUS
                        )
                    )
                    .background(if (isHighlight) tertiaryContainer else primaryContainer)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryDonut(proportions: List<Float>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        DonutChart(
            proportions = proportions,
            colors = ReportUiUtils.getCategoryColors()
        )
        Text(
            "100%",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun DonutChart(proportions: List<Float>, colors: List<Color>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        var startAngle = DONUT_START_ANGLE
        proportions.forEachIndexed { index, proportion ->
            val sweepAngle = proportion * FULL_CIRCLE_DEGREES
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = DONUT_STOKE_WIDTH, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}
