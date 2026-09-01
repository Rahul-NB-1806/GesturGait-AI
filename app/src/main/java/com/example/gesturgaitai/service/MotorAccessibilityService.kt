package com.example.gesturgaitai.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.gesturgaitai.core.OfflineStorage
import com.example.gesturgaitai.core.UnifiedFeatureWindow
import com.example.gesturgaitai.sensor.MotionSensorManager
import com.example.gesturgaitai.sensor.SensorFeatureExtractor
import com.example.gesturgaitai.service.AccessibilityFeatureExtractor
import kotlinx.coroutines.*

class MotorAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "GesturGaitFeatures"
        var isRunning = false
            private set
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var motionSensorManager: MotionSensorManager? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.i(TAG, "!!! GESTURGAIT ACCESSIBILITY SERVICE CONNECTED !!!")

        // Initialize and start motion sensors for 24/7 background monitoring
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        motionSensorManager = MotionSensorManager(sensorManager) { samples ->
            val features = SensorFeatureExtractor.extract(samples)
            val window = UnifiedFeatureWindow.fromSensor(features)
            OfflineStorage.saveWindow(window)

            Log.v(TAG, "Background Motion Window Saved: steps=${features.stepCount}")
        }
        motionSensorManager?.start()
        Log.i(TAG, "Background Passive Monitoring: Sensors Started")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Immediate feedback for screen interaction verification
        if (event.eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START || 
            event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            Log.v(TAG, "Screen Interaction Detected: type=${AccessibilityEvent.eventTypeToString(event.eventType)}")
        }

        AccessibilityFeatureExtractor.processEvent(event)
    }

    override fun onInterrupt() {
        motionSensorManager?.stop()
        stopSelf()
    }

    override fun onDestroy() {
        isRunning = false
        motionSensorManager?.stop()
        scope.cancel()
        AccessibilityFeatureExtractor.reset()
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isRunning = false
        motionSensorManager?.stop()
        return super.onUnbind(intent)
    }
}
