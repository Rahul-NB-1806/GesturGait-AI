package com.example.gesturgaitai.core

import com.example.gesturgaitai.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * SyncCoordinator: Deep module responsible for data aggregation, 
 * on-device inference, and cloud synchronization.
 */
object SyncCoordinator {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    data class SyncState(
        val inferenceResult: InferenceEngine.InferenceResult?,
        val baseline: Baseline?,
        val dailySteps: Int,
        val baselineDays: Int,
        val dailyGroups: Map<String, List<UnifiedFeatureWindow>>
    )

    fun refresh(onResult: (SyncState) -> Unit) {
        scope.launch {
            val storedWindows = OfflineStorage.getWindows()
            val dailyGroups = OfflineStorage.getWindowsGroupedByDay()
            val baselineDays = dailyGroups.size
            
            val todayStr = sdf.format(Date())
            val dailySteps = dailyGroups[todayStr]?.sumOf { it.stepCount } ?: 0
            
            val baseline = OfflineStorage.loadBaseline()
            var finalInferenceResult: InferenceEngine.InferenceResult? = null
            var finalBaseline = baseline

            if (baseline != null) {
                val todayWindows = dailyGroups[todayStr] ?: emptyList()
                val result = InferenceEngine.run(baseline, todayWindows)
                finalInferenceResult = result

                if (result.score > 0 && todayWindows.size >= 5) {
                    val sd = OfflineStorage.StoredScore(
                        score = result.score,
                        confidence = result.confidence,
                        explanation = result.explanation,
                        recommendation = result.recommendation,
                        timestamp = System.currentTimeMillis()
                    )
                    OfflineStorage.saveScore(sd)

                    // Sync with MongoDB
                    val patientId = OfflineStorage.getPatientId() ?: "UNKNOWN"
                    val featureMap = aggregateDailyFeatures(todayWindows)

                    ApiClient.syncScore(
                        userId = patientId,
                        date = todayStr,
                        score = sd.score,
                        confidence = sd.confidence,
                        stepCount = dailySteps,
                        features = featureMap,
                        deviations = result.deviations.map {
                            com.example.gesturgaitai.model.Deviation(it.feature, it.deltaPercent, it.direction)
                        },
                        explanation = sd.explanation,
                        recommendation = sd.recommendation
                    )
                }
            } else if (dailyGroups.size >= 5) {
                val newBaseline = AdaptiveBaseline.buildInitial(storedWindows)
                finalBaseline = newBaseline
                OfflineStorage.saveBaseline(newBaseline)
            }

            onResult(
                SyncState(
                    inferenceResult = finalInferenceResult,
                    baseline = finalBaseline,
                    dailySteps = dailySteps,
                    baselineDays = baselineDays,
                    dailyGroups = dailyGroups
                )
            )
        }
    }

    private fun aggregateDailyFeatures(windows: List<UnifiedFeatureWindow>): Map<String, Double> {
        val keys = listOf("stepCount", "avgStepTime", "peakFrequency", "tremorFrequency",
            "movementStability", "swipeSpeed", "tapInterval", "gestureDurationVariance",
            "tapCount", "scrollDistance")
        val featureMap = mutableMapOf<String, Double>()
        
        for (key in keys) {
            val values = windows.map { it.toFeatureMap()[key] ?: 0.0 }.filter { it > 0.0 }
            if (values.isNotEmpty()) {
                featureMap[key] = values.average()
            }
        }
        return featureMap
    }
}
