package com.example.gesturgaitai.core

import com.example.gesturgaitai.sensor.SensorFeatures
import com.example.gesturgaitai.service.AccessibilityFeatureWindow

data class UnifiedFeatureWindow(
    val timestamp: Long,
    val stepCount: Int = 0,
    val avgStepTime: Double = 0.0,
    val peakFrequency: Double = 0.0,
    val tremorFrequency: Double = 0.0,
    val movementStability: Double = 0.0,
    val swipeSpeed: Double = 0.0,
    val tapInterval: Double = 0.0,
    val gestureDurationVariance: Double = 0.0,
    val tapCount: Int = 0,
    val scrollDistance: Double = 0.0,
    val source: String = "combined"
) {
    fun toFeatureMap(): Map<String, Double> = mapOf(
        "stepCount" to stepCount.toDouble(),
        "avgStepTime" to avgStepTime,
        "peakFrequency" to peakFrequency,
        "tremorFrequency" to tremorFrequency,
        "movementStability" to movementStability,
        "swipeSpeed" to swipeSpeed,
        "tapInterval" to tapInterval,
        "gestureDurationVariance" to gestureDurationVariance,
        "tapCount" to tapCount.toDouble(),
        "scrollDistance" to scrollDistance
    )

    companion object {
        fun fromSensor(sensor: SensorFeatures, timestamp: Long = System.currentTimeMillis()) =
            UnifiedFeatureWindow(
                timestamp = timestamp,
                stepCount = sensor.stepCount,
                avgStepTime = sensor.avgStepTime,
                peakFrequency = sensor.peakFrequency,
                tremorFrequency = sensor.tremorFrequency,
                movementStability = sensor.movementStability,
                source = "sensor"
            )

        fun fromAccessibility(acc: AccessibilityFeatureWindow, timestamp: Long = System.currentTimeMillis()) =
            UnifiedFeatureWindow(
                timestamp = timestamp,
                swipeSpeed = acc.swipeSpeed,
                tapInterval = acc.tapInterval,
                gestureDurationVariance = acc.gestureDurationVariance,
                tapCount = acc.tapCount,
                scrollDistance = acc.scrollDistance,
                source = "accessibility"
            )

        fun merge(sensor: UnifiedFeatureWindow, acc: UnifiedFeatureWindow): UnifiedFeatureWindow {
            val ts = maxOf(sensor.timestamp, acc.timestamp)
            return UnifiedFeatureWindow(
                timestamp = ts,
                stepCount = sensor.stepCount,
                avgStepTime = sensor.avgStepTime,
                peakFrequency = sensor.peakFrequency,
                tremorFrequency = sensor.tremorFrequency,
                movementStability = sensor.movementStability,
                swipeSpeed = acc.swipeSpeed,
                tapInterval = acc.tapInterval,
                gestureDurationVariance = acc.gestureDurationVariance,
                tapCount = acc.tapCount,
                scrollDistance = acc.scrollDistance,
                source = "combined"
            )
        }
    }
}
