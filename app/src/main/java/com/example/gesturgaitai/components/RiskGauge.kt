package com.example.gesturgaitai.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gesturgaitai.ui.theme.*

@Composable
fun RiskGauge(
    score: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Float = 16f
) {
    val scoreColor = when {
        score == null -> MutedLight
        score < 30 -> Success
        score < 60 -> Warning
        else -> Danger
    }
    val label = when {
        score == null -> "--"
        score < 20 -> "Low"
        score < 40 -> "Moderate"
        score < 65 -> "Elevated"
        else -> "High"
    }
    val gaugeTrack = GaugeTrackLight
    val progress = if (score != null) score.coerceIn(0, 100) / 100f else 0f
    val sweepAngle = 270f * progress
    val startAngle = 135f

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            val arcSize = Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = gaugeTrack,
                startAngle = startAngle,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )

            if (score != null) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Success, Warning, Danger),
                        center = Offset(size.toPx() / 2, size.toPx() / 2)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (score != null) score.toString() else "--",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
