package com.example.gesturgaitai.core

import java.util.*

object SampleData {

    fun inject() {
        // 1. Generate 7 days of scores for Trends
        val scores = mutableListOf<OfflineStorage.StoredScore>()
        val cal = Calendar.getInstance()
        
        val random = Random()
        
        for (i in 0 until 14) { // 2 weeks of data
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val score = 15 + random.nextInt(30)
            scores.add(
                OfflineStorage.StoredScore(
                    score = score,
                    confidence = 85.0 + random.nextDouble() * 10.0,
                    explanation = "AI Model Analysis: Your neurological risk score is moderate. Movement stability decreased by ${5 + random.nextInt(10)}% from your baseline.",
                    recommendation = "No immediate action needed. Continue monitoring.",
                    timestamp = cal.timeInMillis
                )
            )
        }
        
        // Save to storage
        scores.forEach { OfflineStorage.saveScore(it) }

        // 2. Generate Feature Windows for Dashboard (Steps)
        val windows = mutableListOf<UnifiedFeatureWindow>()
        cal.time = Date() // Reset to today
        
        // Generate steps for the last 14 days
        for (d in 0 until 14) {
            val dayTimestamp = System.currentTimeMillis() - (d * 86400000L)
            val stepsPerDay = 3000 + random.nextInt(4000)
            val windowsCount = 100 // Assume 100 5-second windows of activity
            
            for (w in 0 until windowsCount) {
                windows.add(
                    UnifiedFeatureWindow(
                        timestamp = dayTimestamp + (w * 60000L), // spread over hours
                        stepCount = stepsPerDay / windowsCount,
                        source = "sensor"
                    )
                )
            }
        }
        
        OfflineStorage.saveWindows(windows)
        
        // 3. Create a fake baseline
        val baseline = Baseline(
            means = mapOf(
                "stepCount" to 45.0,
                "avgStepTime" to 0.55,
                "tremorFrequency" to 0.12,
                "movementStability" to 1.2
            ),
            stdDevs = mapOf(
                "stepCount" to 5.0,
                "avgStepTime" to 0.05,
                "tremorFrequency" to 0.02,
                "movementStability" to 0.15
            ),
            sampleCount = 500,
            establishedAt = System.currentTimeMillis() - 1209600000L // 2 weeks ago
        )
        OfflineStorage.saveBaseline(baseline)
    }
}
