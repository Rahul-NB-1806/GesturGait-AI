package com.example.gesturgaitai.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gesturgaitai.model.Deviation
import com.example.gesturgaitai.ui.theme.*

@Composable
fun DeviationList(
    deviations: List<Deviation>?,
    modifier: Modifier = Modifier
) {
    if (deviations.isNullOrEmpty()) return

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text("Feature Deviations", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))

        deviations.forEachIndexed { i, d ->
            val isWorse = d.direction == "worse"
            val color = if (isWorse) Danger else Success
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = d.feature, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isWorse) "\u2193" else "\u2191",
                        color = color,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${if (d.deltaPercent > 0) "+" else ""}${String.format("%.1f", d.deltaPercent)}%",
                        color = color,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (i < deviations.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
        }
    }
}
