package com.example.gesturgaitai.core

object DataQualityChecker {

    private const val MIN_SENSOR_WINDOWS_PER_DAY = 50
    private const val MIN_ACCESSIBILITY_EVENTS_PER_WINDOW = 2
    private const val MIN_FEATURES_PRESENT = 3

    data class QualityReport(
        val passed: Boolean,
        val sensorWindowsCount: Int,
        val accessibilityEventsCount: Int,
        val featuresPresent: Int,
        val reason: String = ""
    )

    fun check(windows: List<UnifiedFeatureWindow>): QualityReport {
        val validWindows = windows.filter { w ->
            w.source == "combined" || w.source == "sensor"
        }

        val sensorCount = validWindows.size
        val maxAccessibilityEvents = windows.maxOfOrNull { it.tapCount } ?: 0

        val allKeys = mutableSetOf<String>()
        val featureKeys = listOf(
            "stepCount", "avgStepTime", "tremorFrequency", "movementStability",
            "swipeSpeed", "tapInterval"
        )

        for (w in windows) {
            val map = w.toFeatureMap()
            for (key in featureKeys) {
                val v = map[key] ?: 0.0
                if (v > 0.0) allKeys.add(key)
            }
        }

        val featuresPresent = allKeys.size

        val passed = sensorCount >= MIN_SENSOR_WINDOWS_PER_DAY ||
                (sensorCount >= 10 && maxAccessibilityEvents >= MIN_ACCESSIBILITY_EVENTS_PER_WINDOW && featuresPresent >= MIN_FEATURES_PRESENT)

        val reason = if (!passed) when {
            sensorCount < MIN_SENSOR_WINDOWS_PER_DAY && sensorCount < 10 ->
                "Insufficient data: only $sensorCount sensor windows (need $MIN_SENSOR_WINDOWS_PER_DAY)"
            featuresPresent < MIN_FEATURES_PRESENT ->
                "Not enough feature types detected ($featuresPresent of $MIN_FEATURES_PRESENT)"
            else -> "Insufficient accessibility events"
        } else ""

        return QualityReport(passed, sensorCount, maxAccessibilityEvents, featuresPresent, reason)
    }

    fun isBaselineReady(dailyWindows: List<List<UnifiedFeatureWindow>>): Boolean {
        if (dailyWindows.size < 5) return false
        val daysWithData = dailyWindows.count { it.isNotEmpty() && check(it).passed }
        return daysWithData >= 5
    }
}
