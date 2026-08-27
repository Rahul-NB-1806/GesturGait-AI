package com.example.gesturgaitai.screens

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gesturgaitai.core.OfflineStorage
import com.example.gesturgaitai.core.UnifiedFeatureWindow
import com.example.gesturgaitai.sensor.MotionSensorManager
import com.example.gesturgaitai.sensor.SensorFeatureExtractor
import com.example.gesturgaitai.service.AccessibilityFeatureExtractor
import com.example.gesturgaitai.service.MotorAccessibilityService
import com.example.gesturgaitai.service.OemBatteryOptimization
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var accessibilityEnabled by remember { mutableStateOf(false) }
    var sensorWindows by remember { mutableStateOf(0) }
    var accWindows by remember { mutableStateOf(0) }
    var lastSensorFeatures by remember { mutableStateOf("") }
    var lastAccFeatures by remember { mutableStateOf("") }
    var lastCombinedScore by remember { mutableStateOf(0) }

    var sensorManager by remember { mutableStateOf<MotionSensorManager?>(null) }

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
        val sensorMgr = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mgr = MotionSensorManager(sensorMgr) { samples ->
            val features = SensorFeatureExtractor.extract(samples)
            sensorWindows++
            val window = UnifiedFeatureWindow.fromSensor(features)
            OfflineStorage.saveWindow(window)
            lastSensorFeatures = "Steps: ${features.stepCount}, " +
                    "StepTime: ${"%.2f".format(features.avgStepTime)}s, " +
                    "PeakFreq: ${"%.1f".format(features.peakFrequency)}Hz, " +
                    "Tremor: ${"%.2f".format(features.tremorFrequency)}, " +
                    "Stability: ${"%.2f".format(features.movementStability)}"
        }
        mgr.start()
        sensorManager = mgr
    }

    LaunchedEffect(Unit) {
        AccessibilityFeatureExtractor.onWindowReady = { accWindow ->
            accWindows++
            val window = UnifiedFeatureWindow.fromAccessibility(accWindow)
            OfflineStorage.saveWindow(window)
            lastAccFeatures = "Swipe: ${"%.1f".format(accWindow.swipeSpeed)} px/s, " +
                    "TapInterval: ${"%.0f".format(accWindow.tapInterval)} ms, " +
                    "GestureVar: ${"%.1f".format(accWindow.gestureDurationVariance)}, " +
                    "Taps: ${accWindow.tapCount}"
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            accessibilityEnabled = checkAccessibilityService()
            val scores = OfflineStorage.getScores()
            if (scores.isNotEmpty()) lastCombinedScore = scores.first().score
            delay(3000)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sensorManager?.stop()
        }
    }

    val needsBatteryOpt = OemBatteryOptimization.needsOemPrompt()
    val isIgnoringBattery = OemBatteryOptimization.isIgnoringBatteryOptimizations(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitoring", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(8.dp))

            // Accessibility Service Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Accessibility Service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = if (accessibilityEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ) {}
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (accessibilityEnabled) "Active" else "Disabled",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (!accessibilityEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) { Text("Open Accessibility Settings") }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Enable \"GesturGait AI\" in Installed Apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sensor Status
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Motion Sensors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = if (sensorWindows > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {}
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (sensorWindows > 0) "Collecting (${sensorWindows} windows)" else "Waiting for data...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (lastSensorFeatures.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(lastSensorFeatures, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Accessibility Data
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Touch & Gesture Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Windows captured: $accWindows", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    if (lastAccFeatures.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(lastAccFeatures, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Baseline Status
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Baseline Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    val hasBaseline = com.example.gesturgaitai.core.OfflineStorage.hasBaseline()
                    Text(
                        text = if (hasBaseline) "Baseline established \u2713" else "Collecting data for baseline (need 5 days)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Latest Score: $lastCombinedScore",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // OEM Battery Optimization
            if (needsBatteryOpt && !isIgnoringBattery) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Battery Optimization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Disable battery optimization for reliable background data capture.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { OemBatteryOptimization.openOemBatterySettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) { Text("Open App Settings") }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
