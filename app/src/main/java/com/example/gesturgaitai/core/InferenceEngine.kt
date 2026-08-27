package com.example.gesturgaitai.core

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel

object InferenceEngine {

    private const val TFLITE_MODEL = "model.tflite"
    private var interpreter: Interpreter? = null
    private var tfliteAvailable = false

    data class InferenceResult(
        val score: Int,
        val confidence: Double,
        val explanation: String,
        val recommendation: String,
        val deviations: List<DeviationResult>,
        val tfliteAvailable: Boolean = false
    )

    fun initialize(context: Context) {
        try {
            val modelFile = File(context.filesDir, TFLITE_MODEL)
            if (modelFile.exists()) {
                interpreter = Interpreter(modelFile)
                tfliteAvailable = true
            } else {
                // Try loading from assets
                context.assets.openFd(TFLITE_MODEL).use { ad ->
                    FileInputStream(ad.fileDescriptor).use { isStream ->
                        val fileChannel = isStream.channel
                        val startOffset = ad.startOffset
                        val declaredLength = ad.declaredLength
                        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
                        interpreter = Interpreter(modelBuffer)
                        tfliteAvailable = true
                    }
                }
            }
        } catch (_: Exception) {
            tfliteAvailable = false
        }
    }

    fun run(
        baseline: Baseline,
        windows: List<UnifiedFeatureWindow>
    ): InferenceResult {
        if (tfliteAvailable && interpreter != null && windows.isNotEmpty()) {
            try {
                return runTFLite(baseline, windows)
            } catch (e: Exception) {
                // Fall through to statistical
            }
        }

        return runStatistical(baseline, windows)
    }

    private fun runStatistical(
        baseline: Baseline,
        windows: List<UnifiedFeatureWindow>
    ): InferenceResult {
        val quality = DataQualityChecker.check(windows)
        if (!quality.passed) {
            return InferenceResult(
                score = 0,
                confidence = 0.0,
                explanation = "Insufficient data to compute a reliable score",
                recommendation = "Continue using the app normally. More data is needed.",
                deviations = emptyList(),
                tfliteAvailable = false
            )
        }

        val scoreResult = DeviationEngine.score(baseline, windows)
        val explanation = ExplanationGenerator.generate(scoreResult)

        return InferenceResult(
            score = scoreResult.score,
            confidence = scoreResult.confidence,
            explanation = explanation,
            recommendation = scoreResult.recommendation,
            deviations = scoreResult.deviations.map {
                DeviationResult(it.feature, it.deltaPercent, it.direction, it.zScore)
            },
            tfliteAvailable = false
        )
    }

    private fun runTFLite(
        baseline: Baseline,
        windows: List<UnifiedFeatureWindow>
    ): InferenceResult {
        val scoreResult = DeviationEngine.score(baseline, windows)
        
        // Prepare input: normalized features
        val input = FloatArray(10)
        val featureKeys = listOf(
            "stepCount", "avgStepTime", "peakFrequency", "tremorFrequency",
            "movementStability", "swipeSpeed", "tapInterval", "gestureDurationVariance",
            "tapCount", "scrollDistance"
        )
        
        val deviationsMap = scoreResult.deviations.associateBy { it.feature }
        for (i in featureKeys.indices) {
            input[i] = deviationsMap[featureKeys[i]]?.zScore?.toFloat() ?: 0.0f
        }
        
        val output = Array(1) { FloatArray(1) }
        interpreter?.run(arrayOf(input), output)
        
        val modelScore = (output[0][0] * 100).toInt().coerceIn(0, 100)
        val explanation = ExplanationGenerator.generate(scoreResult)

        return InferenceResult(
            score = modelScore,
            confidence = (scoreResult.confidence + 15.0).coerceAtMost(99.0),
            explanation = "AI Model Analysis: $explanation",
            recommendation = scoreResult.recommendation,
            deviations = scoreResult.deviations.map {
                DeviationResult(it.feature, it.deltaPercent, it.direction, it.zScore)
            },
            tfliteAvailable = true
        )
    }

    data class DeviationResult(
        val feature: String,
        val deltaPercent: Double,
        val direction: String,
        val zScore: Double
    )
}
