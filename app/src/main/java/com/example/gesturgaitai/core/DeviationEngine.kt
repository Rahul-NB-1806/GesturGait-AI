package com.example.gesturgaitai.core

data class DeviationResult(
    val feature: String,
    val deltaPercent: Double,
    val direction: String,
    val zScore: Double
)

object DeviationEngine {

    private val FEATURE_WEIGHTS = mapOf(
        "stepCount" to 1.0,
        "avgStepTime" to 1.2,
        "peakFrequency" to 1.0,
        "tremorFrequency" to 1.5,
        "movementStability" to 1.3,
        "swipeSpeed" to 1.0,
        "tapInterval" to 1.1,
        "gestureDurationVariance" to 1.0,
        "tapCount" to 0.8,
        "scrollDistance" to 0.6
    )

    data class ScoreResult(
        val score: Int,
        val confidence: Double,
        val deviations: List<DeviationResult>,
        val explanation: String,
        val recommendation: String
    )

    fun score(
        baseline: Baseline,
        windows: List<UnifiedFeatureWindow>
    ): ScoreResult {
        val avgFeatures = aggregate(windows)
        val deviations = mutableListOf<DeviationResult>()
        var totalWeightedZ = 0.0
        var totalWeight = 0.0

        for ((key, value) in avgFeatures) {
            val mean = baseline.means[key] ?: continue
            if (mean <= 0.0) continue

            val std = baseline.stdDevs[key]
            val ema = baseline.emaValues[key] ?: mean

            val deltaPercent = if (mean > 0.0) ((value - mean) / mean) * 100.0 else 0.0

            val zScore: Double
            val direction: String
            val isBetter : Boolean

            if (std != null && std > 0.0) {
                zScore = (value - mean) / std
            } else {
                zScore = deltaPercent / 100.0
            }

            // For most features, increase = worse (except stepCount where more steps = better)
            if (key == "stepCount" || key == "tapCount") {
                direction = if (deltaPercent < -10) "worse" else "better"
                isBetter = deltaPercent >= -10
            } else {
                direction = if (deltaPercent > 10) "worse" else "better"
                isBetter = deltaPercent <= 10
            }

            val weight = FEATURE_WEIGHTS[key] ?: 1.0
            totalWeightedZ += abs(zScore) * weight
            totalWeight += weight

            deviations.add(
                DeviationResult(
                    feature = key,
                    deltaPercent = roundTo1(deltaPercent),
                    direction = direction,
                    zScore = roundTo2(zScore)
                )
            )
        }

        val avgZ = if (totalWeight > 0.0) totalWeightedZ / totalWeight else 0.0
        val rawScore = (avgZ * 20).toInt().coerceIn(0, 100)

        val significantDeviations = deviations.filter {
            abs(it.deltaPercent) > 10 || abs(it.zScore) > 1.5
        }

        val sortedDeviations = significantDeviations.sortedByDescending {
            abs(it.zScore)
        }

        val explanation = if (sortedDeviations.isNotEmpty()) {
            sortedDeviations.take(3).joinToString(", ") { d ->
                val dir = if (d.direction == "worse") "decreased" else "increased"
                "${d.feature} $dir ${abs(d.deltaPercent).toInt()}% from your baseline"
            }
        } else {
            "All metrics are within normal range compared to your baseline"
        }

        val recommendation = when {
            rawScore < 20 -> "No immediate action needed. Continue monitoring as usual."
            rawScore < 40 -> "Minor deviations detected. Monitor for trends over the coming days."
            rawScore < 65 -> "Moderate changes detected. Consider consulting a neurologist if trends continue."
            else -> "Significant changes detected. We recommend consulting a neurologist."
        }

        val confidence = computeConfidence(baseline, windows.size)

        return ScoreResult(
            score = rawScore,
            confidence = confidence,
            deviations = sortedDeviations,
            explanation = explanation,
            recommendation = recommendation
        )
    }

    private fun aggregate(windows: List<UnifiedFeatureWindow>): Map<String, Double> {
        if (windows.isEmpty()) return emptyMap()
        val sums = mutableMapOf<String, Double>()
        val counts = mutableMapOf<String, Int>()
        val keys = listOf("stepCount", "avgStepTime", "peakFrequency", "tremorFrequency",
            "movementStability", "swipeSpeed", "tapInterval", "gestureDurationVariance",
            "tapCount", "scrollDistance")

        for (w in windows) {
            val map = w.toFeatureMap()
            for (key in keys) {
                val v = map[key] ?: 0.0
                if (v > 0.0) {
                    sums[key] = (sums[key] ?: 0.0) + v
                    counts[key] = (counts[key] ?: 0) + 1
                }
            }
        }

        val result = mutableMapOf<String, Double>()
        for (key in keys) {
            val count = counts[key] ?: 0
            if (count > 0) {
                result[key] = (sums[key] ?: 0.0) / count
            }
        }
        return result
    }

    private fun computeConfidence(baseline: Baseline, windowCount: Int): Double {
        val sampleRatio = (baseline.sampleCount.coerceAtLeast(1) / 1000.0).coerceAtMost(1.0)
        val windowRatio = (windowCount / 50.0).coerceAtMost(1.0)
        val baseConfidence = 0.5 + (sampleRatio * 0.3) + (windowRatio * 0.2)
        return roundTo2(baseConfidence.coerceIn(0.0, 1.0) * 100.0)
    }

    private fun abs(x: Double) = if (x < 0) -x else x
    private fun roundTo1(x: Double) = Math.round(x * 10.0) / 10.0
    private fun roundTo2(x: Double) = Math.round(x * 100.0) / 100.0
}
