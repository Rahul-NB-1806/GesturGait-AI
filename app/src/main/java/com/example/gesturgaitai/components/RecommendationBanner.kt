package com.example.gesturgaitai.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gesturgaitai.ui.theme.*

@Composable
fun RecommendationBanner(
    text: String?,
    score: Int?,
    modifier: Modifier = Modifier
) {
    if (text.isNullOrBlank()) return

    val (bgColor, iconColor, icon) = when {
        score != null && score >= 60 -> Triple(Color(0xFFFFF5F5), Danger, "\u26A0")
        score != null && score >= 30 -> Triple(Color(0xFFFFFFF0), Warning, "\u2139")
        else -> Triple(Color(0xFFEBF8FF), MedicalBlue, "\u2139")
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium, color = iconColor)
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = iconColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
