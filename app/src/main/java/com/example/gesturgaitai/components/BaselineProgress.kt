package com.example.gesturgaitai.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.gesturgaitai.ui.theme.*

@Composable
fun BaselineProgress(
    daysCollected: Int,
    daysRequired: Int,
    modifier: Modifier = Modifier
) {
    val progress = (daysCollected.toFloat() / daysRequired.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "baseline"
    )

    val bgColor = MedicalBlueLight
    val fgColor = MedicalBlue
    val trackColor = BorderLight

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(bgColor)
            .padding(20.dp)
    ) {
        Text(
            text = "Establishing your baseline...",
            style = MaterialTheme.typography.titleMedium,
            color = fgColor
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(fgColor)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Day $daysCollected of $daysRequired",
            style = MaterialTheme.typography.bodyMedium,
            color = fgColor
        )
    }
}
