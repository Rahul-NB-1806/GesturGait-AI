package com.example.gesturgaitai.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gesturgaitai.components.*
import com.example.gesturgaitai.core.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var inferenceResult by remember { mutableStateOf<InferenceEngine.InferenceResult?>(null) }
    var baseline by remember { mutableStateOf<Baseline?>(null) }
    var loading by remember { mutableStateOf(true) }
    var trendView by remember { mutableStateOf("daily") }
    var baselineDays by remember { mutableStateOf(0) }

    fun runInference() {
        val storedWindows = OfflineStorage.getWindows()
        val dailyGroups = OfflineStorage.getWindowsGroupedByDay()
        baselineDays = dailyGroups.size

        val loaded = OfflineStorage.loadBaseline()

        if (loaded != null) {
            baseline = loaded
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val todayWindows = dailyGroups[todayStr] ?: emptyList()
            val result = InferenceEngine.run(loaded, todayWindows)
            inferenceResult = result

            if (result.score > 0 && todayWindows.size >= 5) {
                OfflineStorage.saveScore(
                    OfflineStorage.StoredScore(
                        score = result.score,
                        confidence = result.confidence,
                        explanation = result.explanation,
                        recommendation = result.recommendation,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        } else if (dailyGroups.size >= 5) {
            val allWindows = storedWindows
            val newBaseline = AdaptiveBaseline.buildInitial(allWindows)
            baseline = newBaseline
            OfflineStorage.saveBaseline(newBaseline)
        }
        loading = false
    }

    LaunchedEffect(Unit) { runInference() }

    val scores = OfflineStorage.getScores()
    val scoreValues = scores.map { it.score }
    val avgScore = if (scoreValues.isNotEmpty()) scoreValues.average().toInt() else null
    val bestScore = if (scoreValues.isNotEmpty()) scoreValues.minOrNull() else null
    val worstScore = if (scoreValues.isNotEmpty()) scoreValues.maxOrNull() else null

    val scoreValue = inferenceResult?.score
    val confidence = inferenceResult?.confidence

    val trendData: List<Pair<String, Int>> = when (trendView) {
        "daily" -> scores.takeLast(7).map {
            val sdf = SimpleDateFormat("EEE", Locale.US)
            Pair(sdf.format(Date(it.timestamp)), it.score)
        }
        "weekly" -> {
            val grouped = scores.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.WEEK_OF_YEAR)
            }
            grouped.map { (week, list) ->
                Pair("W$week", list.map { it.score }.average().toInt())
            }.takeLast(4)
        }
        else -> {
            val grouped = scores.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.MONTH)
            }
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            grouped.map { (month, list) ->
                Pair(months.getOrElse(month) { "$month" }, list.map { it.score }.average().toInt())
            }.takeLast(6)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { loading = true; runInference() }) {
                        Text("Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text(text = if (baselineDays < 5) "Building baseline... Day $baselineDays of 5" else "Analyzing...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(8.dp))

            if (baseline == null && baselineDays < 5) {
                BaselineProgress(
                    daysCollected = baselineDays.coerceAtMost(7),
                    daysRequired = 7
                )
            } else {
                ScoreCard(
                    score = scoreValue,
                    explanation = inferenceResult?.explanation,
                    recommendation = inferenceResult?.recommendation
                )
            }

            if (confidence != null && scoreValue != null) {
                Text(
                    text = "Confidence: ${confidence.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            PeriodToggle(selected = trendView, onSelect = { trendView = it })

            if (trendData.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Trend", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        trendData.forEachIndexed { i, (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$value", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("No trend data yet. Keep using the app.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            SummaryStats(avg = avgScore, best = bestScore, worst = worstScore)

            val currentResult = inferenceResult
            if (currentResult?.deviations?.isNotEmpty() == true) {
                DeviationList(
                    deviations = currentResult.deviations.map {
                        com.example.gesturgaitai.model.Deviation(
                            feature = it.feature,
                            deltaPercent = it.deltaPercent,
                            direction = it.direction
                        )
                    }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
