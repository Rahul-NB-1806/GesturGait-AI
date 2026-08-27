package com.example.gesturgaitai.core

object ExplanationGenerator {

    fun generate(scoreResult: DeviationEngine.ScoreResult): String {
        val parts = mutableListOf<String>()

        val riskLabel = when {
            scoreResult.score < 20 -> "Your neurological risk score is low"
            scoreResult.score < 40 -> "Your neurological risk score is moderate"
            scoreResult.score < 65 -> "Your neurological risk score is elevated"
            else -> "Your neurological risk score is high"
        }
        parts.add(riskLabel)

        val confidenceLevel = when {
            scoreResult.confidence >= 80 -> "high confidence"
            scoreResult.confidence >= 50 -> "moderate confidence"
            else -> "low confidence (more data needed)"
        }
        parts.add("(based on current data, $confidenceLevel)")

        val worseDevs = scoreResult.deviations.filter { it.direction == "worse" }
        if (worseDevs.isNotEmpty()) {
            val lines = mutableListOf<String>()
            for (d in worseDevs.take(3)) {
                val friendlyName = friendlyFeatureName(d.feature)
                val description = getDeviationDescription(d.feature, d.deltaPercent)
                lines.add("$friendlyName $description")
            }
            parts.add("Notable changes: ${lines.joinToString("; ")}.")
        }

        val betterDevs = scoreResult.deviations.filter { it.direction == "better" }
        if (betterDevs.isNotEmpty() && scoreResult.score < 40) {
            val lines = betterDevs.take(2).map {
                "${friendlyFeatureName(it.feature)} is stable"
            }
            parts.add("Positive indicators: ${lines.joinToString(", ")}.")
        }

        parts.add(scoreResult.recommendation)

        return parts.joinToString(" ")
    }

    private fun getDeviationDescription(feature: String, delta: Double): String {
        val absDelta = abs(delta).toInt()
        val dir = if (delta > 0) "increased" else "decreased"
        
        return when (feature) {
            "tremorFrequency" -> "showed $absDelta% more tremor activity, which can be a key indicator of motor changes"
            "movementStability" -> "stability $dir by $absDelta%, affecting gait consistency"
            "avgStepTime" -> "step timing $dir by $absDelta%, possibly indicating changes in walking pace"
            "swipeSpeed" -> "interaction speed $dir by $absDelta%, reflecting changes in fine motor control"
            "tapInterval" -> "response time $dir by $absDelta%, which may correlate with cognitive or motor processing"
            else -> "$dir by $absDelta% from your established baseline"
        }
    }

    private fun friendlyFeatureName(key: String): String = when (key) {
        "stepCount" -> "Step count"
        "avgStepTime" -> "Step timing"
        "peakFrequency" -> "Movement rhythm"
        "tremorFrequency" -> "Tremor frequency"
        "movementStability" -> "Movement stability"
        "swipeSpeed" -> "Swipe speed"
        "tapInterval" -> "Tap interval"
        "gestureDurationVariance" -> "Gesture consistency"
        "tapCount" -> "Touch interaction"
        "scrollDistance" -> "Scroll behavior"
        else -> key
    }

    private fun abs(x: Double) = if (x < 0) -x else x
}
