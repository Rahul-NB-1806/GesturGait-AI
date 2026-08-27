package com.example.gesturgaitai.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityRecord
import com.example.gesturgaitai.service.AccessibilityFeatureExtractor
import kotlinx.coroutines.*

class MotorAccessibilityService : AccessibilityService() {

    companion object {
        var isRunning = false
            private set
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        AccessibilityFeatureExtractor.processEvent(event)
    }

    override fun onInterrupt() {
        stopSelf()
    }

    override fun onDestroy() {
        isRunning = false
        scope.cancel()
        AccessibilityFeatureExtractor.reset()
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isRunning = false
        return super.onUnbind(intent)
    }
}
