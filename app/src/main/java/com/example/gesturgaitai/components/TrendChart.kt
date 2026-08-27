package com.example.gesturgaitai.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gesturgaitai.model.SummaryItem
import com.example.gesturgaitai.model.ScoreResponse
import com.example.gesturgaitai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrendChart(
    data: List<Any>,
    period: String,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val border = MaterialTheme.colorScheme.outline

    val chartData = data.map { item ->
        when (item) {
            is ScoreResponse -> {
                val dateStr = item.explanation ?: ""
                val label = if (period == "daily") {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        val date = sdf.parse(item.explanation ?: "")
                        SimpleDateFormat("EEE", Locale.US).format(date!!).take(3)
                    } catch (e: Exception) { "" }
                } else ""
                Triple(label, item.score ?: 0, item.score ?: 0)
            }
            is SummaryItem -> Triple(
                when (period) {
                    "weekly" -> "W${item.label.takeLast(2)}"
                    "monthly" -> {
                        val parts = item.label.split("-")
                        if (parts.size >= 2) {
                            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                            val idx = parts[1].toIntOrNull()?.minus(1) ?: 0
                            months.getOrElse(idx) { parts[1] }
                        } else item.label
                    }
                    else -> item.label
                },
                item.avgScore,
                item.avgScore
            )
            else -> Triple("", 0, 0)
        }
    }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text("Trend", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))

        if (chartData.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No data yet", color = muted)
            }
            return
        }

        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val w = size.width
            val h = size.height
            val padL = 40f
            val padR = 16f
            val padT = 16f
            val padB = 32f
            val chartW = w - padL - padR
            val chartH = h - padT - padB

            val values = chartData.map { it.third }
            val minV = (values.minOrNull() ?: 0).coerceAtMost(0)
            val maxV = (values.maxOrNull() ?: 100).coerceAtLeast(100)
            val range = (maxV - minV).coerceAtLeast(1)

            drawLine(color = border, start = Offset(padL, padT), end = Offset(padL, padT + chartH), strokeWidth = 1f)
            drawLine(color = border, start = Offset(padL, padT + chartH), end = Offset(padL + chartW, padT + chartH), strokeWidth = 1f)

            if (chartData.size == 1) return@Canvas

            val stepX = chartW / (chartData.size - 1).coerceAtLeast(1)

            val path = Path()
            chartData.forEachIndexed { i, (_, _, value) ->
                val x = padL + i * stepX
                val y = padT + chartH - ((value - minV) / range * chartH).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(path, color = primary, style = Stroke(width = 3f, cap = StrokeCap.Round))

            if (period == "daily") {
                chartData.forEachIndexed { i, (label, _, value) ->
                    val x = padL + i * stepX
                    val y = padT + chartH - ((value - minV) / range * chartH).toFloat()
                    drawCircle(color = primary, radius = 5f, center = Offset(x, y))
                }
            } else {
                val barW = (stepX * 0.6f).coerceAtMost(32f)
                chartData.forEachIndexed { i, (label, _, value) ->
                    val x = padL + i * stepX - barW / 2
                    val barH = ((value - minV) / range * chartH).toFloat()
                    val y = padT + chartH - barH
                    drawRect(color = primary, topLeft = Offset(x, y), size = Size(barW, barH.coerceAtLeast(1f)))
                }
            }
        }
    }
}
