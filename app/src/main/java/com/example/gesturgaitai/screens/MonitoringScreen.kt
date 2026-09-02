package com.example.gesturgaitai.screens

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import com.example.gesturgaitai.components.*
import com.example.gesturgaitai.core.OfflineStorage
import com.example.gesturgaitai.service.AccessibilityFeatureExtractor
import com.example.gesturgaitai.service.OemBatteryOptimization
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen(onShowTutorial: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var accessibilityEnabled by remember { mutableStateOf(false) }
    var lastSensorFeatures by remember { mutableStateOf("") }
    var lastAccFeatures by remember { mutableStateOf("") }
    var lastCombinedScore by remember { mutableStateOf(0) }

    fun checkAccessibilityService(): Boolean {
        val enabled = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1
        } catch (_: Exception) { false }

        return if (enabled) {
            val serviceStr = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            } catch (_: Exception) { null }
            serviceStr?.contains(context.packageName) == true
        } else false
    }

    LaunchedEffect(Unit) {
        while (true) {
            accessibilityEnabled = checkAccessibilityService()
            val scores = OfflineStorage.getScores()
            if (scores.isNotEmpty()) lastCombinedScore = scores.first().score

            val windows = OfflineStorage.getWindows()
            val sensorWindows = windows.filter { it.source == "sensor" || it.source == "combined" }
            val accWindows = windows.filter { it.source == "accessibility" || it.source == "combined" }

            if (sensorWindows.isNotEmpty()) {
                val last = sensorWindows.last()
                lastSensorFeatures = "Steps: ${last.stepCount}, " +
                        "StepTime: ${"%.2f".format(last.avgStepTime)}s, " +
                        "PeakFreq: ${"%.1f".format(last.peakFrequency)}Hz, " +
                        "Tremor: ${"%.2f".format(last.tremorFrequency)}, " +
                        "Stability: ${"%.2f".format(last.movementStability)}"
            }

            if (accWindows.isNotEmpty()) {
                val last = accWindows.last()
                lastAccFeatures = "Swipe: ${"%.1f".format(last.swipeSpeed)} px/s, " +
                        "TapInterval: ${"%.0f".format(last.tapInterval)} ms, " +
                        "GestureVar: ${"%.1f".format(last.gestureDurationVariance)}, " +
                        "Taps: ${last.tapCount}"
            }

            OfflineStorage.logHealthStatus()
            delay(5000)
        }
    }

    val needsBatteryOpt = OemBatteryOptimization.needsOemPrompt()
    val isIgnoringBattery = OemBatteryOptimization.isIgnoringBatteryOptimizations(context)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Monitor",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(8.dp))

            // Accessibility Service Status
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Accessibility Service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onShowTutorial) {
                        Icon(
                            Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Tutorial",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (accessibilityEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    ) {}
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (accessibilityEnabled) "Active" else "Disabled",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (!accessibilityEnabled) {
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Open System Settings") }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Sensor Status
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Physical Motion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (lastSensorFeatures.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {}
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (lastSensorFeatures.isNotEmpty()) "Collecting Data (Background)" else "Waiting for movement...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (lastSensorFeatures.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        lastSensorFeatures,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Accessibility Data
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Fine Motor Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (lastAccFeatures.isNotEmpty()) "Active Monitoring" else "No recent interactions",
                    style = MaterialTheme.typography.bodyLarge
                )
                if (lastAccFeatures.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        lastAccFeatures,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Baseline Status
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Calibration Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                val hasBaseline = com.example.gesturgaitai.core.OfflineStorage.hasBaseline()
                Text(
                    text = if (hasBaseline) "Established \u2713" else "Calibrating baseline...",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Historical Score: $lastCombinedScore",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // OEM Battery Optimization
            if (needsBatteryOpt && !isIgnoringBattery) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Background Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Enable background data capture to ensure continuous monitoring.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { OemBatteryOptimization.openOemBatterySettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Allow Background Use") }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
