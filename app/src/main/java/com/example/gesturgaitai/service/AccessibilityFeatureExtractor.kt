package com.example.gesturgaitai.service

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlin.math.abs
import java.util.concurrent.ConcurrentLinkedQueue

data class AccessibilityFeatureWindow(
    val swipeSpeed: Double = 0.0,
    val tapInterval: Double = 0.0,
    val gestureDurationVariance: Double = 0.0,
    val tapCount: Int = 0,
    val scrollDistance: Double = 0.0
)

object AccessibilityFeatureExtractor {

    private const val TAG = "GesturGaitFeatures"

    private data class TouchEvent(
        val timestamp: Long,
        val eventType: Int,
        val x: Float = 0f,
        val y: Float = 0f
    )

    private const val WINDOW_MS = 5000L

    private val events = ConcurrentLinkedQueue<TouchEvent>()
    private var lastTapTime = 0L
    private var lastSwipeStartTime = 0L
    private var lastSwipeStartX = 0f
    private var lastSwipeStartY = 0f
    private var totalSwipeDistance = 0.0
    private var swipeCount = 0
    private var gestureDurations = mutableListOf<Long>()
    private var scrollTotalDistance = 0.0

    private var lastWindowTime = System.currentTimeMillis()

    fun processEvent(event: AccessibilityEvent) {
        val now = System.currentTimeMillis()
        if (event.eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START ||
            event.eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_END
        ) {
            // AccessibilityEvent does not provide raw touch coordinates.
            // Using 0f as placeholders for x and y.
            events.add(TouchEvent(now, event.eventType, 0f, 0f))
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_TOUCH_INTERACTION_START -> {
                lastTapTime = now
            }
            AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> {
                val duration = now - lastTapTime
                if (duration < 300) {
                    Log.v(TAG, "Screen Sensor: Tap detected (${duration}ms)")
                } else {
                    swipeCount++
                    Log.v(TAG, "Screen Sensor: Swipe/Long-press detected (${duration}ms)")
                    lastSwipeStartTime = lastTapTime
                }
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val delta = abs(event.scrollDeltaX) + abs(event.scrollDeltaY)
                    scrollTotalDistance += delta
                    totalSwipeDistance += delta
                }
            }
        }

        trimEvents(now)

        val windowElapsed = now - lastWindowTime
        if (windowElapsed >= WINDOW_MS) {
            val window = computeWindow()
            if (window.tapCount > 0 || window.swipeSpeed > 0) {
                Log.d(TAG, "========== GESTURGAIT ACCESSIBILITY WINDOW ==========")
                Log.d(TAG, "swipeSpeed: ${"%.2f".format(window.swipeSpeed)}")
                Log.d(TAG, "tapInterval: ${"%.2f".format(window.tapInterval)}")
                Log.d(TAG, "gestureVar: ${"%.2f".format(window.gestureDurationVariance)}")
                Log.d(TAG, "tapCount: ${window.tapCount}")
                Log.d(TAG, "scrollDistance: ${"%.2f".format(window.scrollDistance)}")
                Log.d(TAG, "======================================================")
                onWindowReady?.invoke(window)
            } else {
                Log.d(TAG, "[GesturGaitFeatures] WARNING: No accessibility events in current window")
            }
            lastWindowTime = now
            gestureDurations.clear()
            totalSwipeDistance = 0.0
            swipeCount = 0
            scrollTotalDistance = 0.0
        }
    }

    private fun trimEvents(now: Long) {
        val cutoff = now - WINDOW_MS
        while (events.peek()?.timestamp != null && events.peek()!!.timestamp < cutoff) {
            events.poll()
        }
    }

    private fun computeWindow(): AccessibilityFeatureWindow {
        val eventList = events.toList()
        if (eventList.size < 2) {
            return AccessibilityFeatureWindow(tapCount = eventList.size)
        }

        val taps = eventList.filter { it.eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_END }
        val tapIntervals = mutableListOf<Long>()
        for (i in 1 until taps.size) {
            tapIntervals.add(taps[i].timestamp - taps[i - 1].timestamp)
        }
        val avgTapInterval = if (tapIntervals.isNotEmpty()) tapIntervals.average() else 0.0

        val totalDuration = eventList.last().timestamp - eventList.first().timestamp
        val avgSwipeSpeed = if (totalDuration > 0 && swipeCount > 0) {
            totalSwipeDistance / (totalDuration / 1000.0)
        } else 0.0

        val avgDuration = if (gestureDurations.isNotEmpty()) gestureDurations.average() else 0.0
        val durationVariance = if (gestureDurations.size > 1) {
            gestureDurations.map { (it - avgDuration) * (it - avgDuration) }.average()
        } else 0.0

        return AccessibilityFeatureWindow(
            swipeSpeed = avgSwipeSpeed,
            tapInterval = avgTapInterval,
            gestureDurationVariance = durationVariance,
            tapCount = eventList.size / 2,
            scrollDistance = scrollTotalDistance
        )
    }

    fun reset() {
        events.clear()
        lastTapTime = 0L
        lastSwipeStartTime = 0L
        totalSwipeDistance = 0.0
        swipeCount = 0
        gestureDurations.clear()
        scrollTotalDistance = 0.0
        lastWindowTime = System.currentTimeMillis()
        onWindowReady = null
    }

    var onWindowReady: ((AccessibilityFeatureWindow) -> Unit)? = null
}
