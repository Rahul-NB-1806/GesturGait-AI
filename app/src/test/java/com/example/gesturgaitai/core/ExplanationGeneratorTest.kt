package com.example.gesturgaitai.core

import org.junit.Test
import org.junit.Assert.*

class ExplanationGeneratorTest {

    @Test
    fun testGenerateLowRisk() {
        val scoreResult = DeviationEngine.ScoreResult(
            score = 15,
            confidence = 85.0,
            deviations = emptyList(),
            explanation = "",
            recommendation = "No immediate action needed."
        )
        val explanation = ExplanationGenerator.generate(scoreResult)
        assertTrue(explanation.contains("low"))
        assertTrue(explanation.contains("high confidence"))
        assertTrue(explanation.contains("No immediate action needed"))
    }

    @Test
    fun testGenerateHighRiskWithDeviations() {
        val deviations = listOf(
            DeviationResult("tremorFrequency", 25.0, "worse", 2.5),
            DeviationResult("movementStability", -15.0, "worse", 1.8)
        )
        val scoreResult = DeviationEngine.ScoreResult(
            score = 75,
            confidence = 90.0,
            deviations = deviations,
            explanation = "",
            recommendation = "Significant changes detected. Consult a neurologist."
        )
        val explanation = ExplanationGenerator.generate(scoreResult)
        assertTrue(explanation.contains("high"))
        assertTrue(explanation.contains("Notable changes"))
        assertTrue(explanation.contains("tremor activity"))
        assertTrue(explanation.contains("stability"))
        assertTrue(explanation.contains("Significant changes detected"))
    }
}
