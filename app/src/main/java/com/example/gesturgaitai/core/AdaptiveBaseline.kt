package com.example.gesturgaitai.core

data class Baseline(
    val means: Map<String, Double> = emptyMap(),
    val stdDevs: Map<String, Double> = emptyMap(),
    val emaValues: Map<String, Double> = emptyMap(),
    val sampleCount: Int = 0,
    val establishedAt: Long = 0L
)

object AdaptiveBaseline {

    private const val EMA_ALPHA = 0.3
    private const val MIN_SAMPLES_FOR_STDDEV = 5
    private val FEATURE_KEYS = listOf(
        "stepCount", "avgStepTime", "peakFrequency", "tremorFrequency",
        "movementStability", "swipeSpeed", "tapInterval", "gestureDurationVariance",
        "tapCount", "scrollDistance"
    )

    fun buildInitial(featureWindows: List<UnifiedFeatureWindow>): Baseline {
        val featureMap = mutableMapOf<String, MutableList<Double>>()
        FEATURE_KEYS.forEach { key -> featureMap[key] = mutableListOf() }

        for (w in featureWindows) {
            val map = w.toFeatureMap()
            for ((key, values) in featureMap) {
                val v = map[key] ?: 0.0
                if (v > 0.0) values.add(v)
            }
        }

        val means = mutableMapOf<String, Double>()
        val stdDevs = mutableMapOf<String, Double>()

        for ((key, values) in featureMap) {
            if (values.size < 2) continue
            val mean = values.average()
            means[key] = mean
            if (values.size >= MIN_SAMPLES_FOR_STDDEV) {
                val variance = values.map { (it - mean) * (it - mean) }.average()
                stdDevs[key] = kotlin.math.sqrt(variance)
            } else {
                stdDevs[key] = 0.0
            }
        }

        return Baseline(
            means = means,
            stdDevs = stdDevs,
            emaValues = means.toMap(),
            sampleCount = featureWindows.size,
            establishedAt = System.currentTimeMillis()
        )
    }

    fun updateEma(current: Baseline, newWindow: UnifiedFeatureWindow): Baseline {
        if (current.emaValues.isEmpty()) return buildInitial(listOf(newWindow))

        val map = newWindow.toFeatureMap()
        val updatedEma = current.emaValues.toMutableMap()

        for ((key, oldEma) in updatedEma) {
            val v = map[key] ?: 0.0
            if (v > 0.0) {
                updatedEma[key] = EMA_ALPHA * v + (1 - EMA_ALPHA) * oldEma
            }
        }

        return current.copy(
            emaValues = updatedEma,
            sampleCount = current.sampleCount + 1
        )
    }

    fun computeDeviation(current: Baseline, newWindow: UnifiedFeatureWindow): Map<String, Double> {
        val deviations = mutableMapOf<String, Double>()
        val map = newWindow.toFeatureMap()

        for ((key, mean) in current.means) {
            val v = map[key] ?: continue
            if (v <= 0.0) continue
            val std = current.stdDevs[key] ?: continue
            if (std <= 0.0) {
                deviations[key] = if (key == "stepCount" || key == "tapCount") 0.0
                else ((v - mean) / mean) * 100.0
                continue
            }
            val z = (v - mean) / std
            deviations[key] = z
        }

        return deviations
    }
}
