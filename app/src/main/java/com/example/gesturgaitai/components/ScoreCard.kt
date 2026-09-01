package com.example.gesturgaitai.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScoreCard(
    score: Int?,
    explanation: String?,
    recommendation: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Risk Analysis",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(24.dp))
        RiskGauge(score = score)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Risk scale: 0 to 100",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!explanation.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (!recommendation.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            RecommendationBanner(text = recommendation, score = score)
        }
    }
}
