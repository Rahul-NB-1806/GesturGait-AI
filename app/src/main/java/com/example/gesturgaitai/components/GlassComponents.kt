package com.example.gesturgaitai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gesturgaitai.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // Very low opacity for that ultra-transparent "Vision" look
    val backgroundColor = if (isDark) Color(0x0DFFFFFF) else Color(0x33FFFFFF) // 5% vs 20%
    val borderColor = if (isDark) Color(0x1AFFFFFF) else Color(0x4DFFFFFF)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            width = 0.5.dp, // Thinner border for high-end look
            brush = Brush.verticalGradient(
                colors = listOf(
                    borderColor, 
                    borderColor.copy(alpha = 0.05f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                content()
            }
        }
    }
}

@Composable
fun AppleBackground(
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // Background requires vibrant colors to make glass transparency visible
    val baseBg = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBg)
    ) {
        // Decorative vibrant "blobs" to create depth behind the glass
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top Left Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0A84FF).copy(alpha = 0.15f), Color.Transparent),
                    center = center.copy(x = 0f, y = 0f),
                    radius = size.width
                )
            )
            // Bottom Right Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF5E5CE6).copy(alpha = 0.12f), Color.Transparent),
                    center = center.copy(x = size.width, y = size.height),
                    radius = size.width
                )
            )
            // Center subtle warmth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF375F).copy(alpha = 0.05f), Color.Transparent),
                    center = center,
                    radius = size.width / 2
                )
            )
        }
        
        content()
    }
}

// Helper to use Canvas in the background
@Composable
fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}
