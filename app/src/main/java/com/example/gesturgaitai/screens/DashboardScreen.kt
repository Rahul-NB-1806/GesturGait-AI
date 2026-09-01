package com.example.gesturgaitai.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gesturgaitai.components.*
import com.example.gesturgaitai.core.*
import com.example.gesturgaitai.network.ApiClient
import com.example.gesturgaitai.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val scrollState = rememberScrollState()

    var syncState by remember { mutableStateOf<SyncCoordinator.SyncState?>(null) }
    var loading by remember { mutableStateOf(true) }
    var trendView by remember { mutableStateOf("daily") }

    fun loadData() {
        loading = true
        SyncCoordinator.refresh { state ->
            syncState = state
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    val scores = OfflineStorage.getScores()
    val scoreValues = scores.map { it.score }
    val avgScore = if (scoreValues.isNotEmpty()) scoreValues.average().toInt() else null
    val bestScore = if (scoreValues.isNotEmpty()) scoreValues.minOrNull() else null
    val worstScore = if (scoreValues.isNotEmpty()) scoreValues.maxOrNull() else null

    val dailyGroupsMap = syncState?.dailyGroups ?: emptyMap()
    val stepTrendData: List<Pair<String, Int>> = when (trendView) {
        "daily" -> {
            val sortedDays = dailyGroupsMap.keys.sorted().takeLast(7)
            sortedDays.map { day ->
                val daySteps = dailyGroupsMap[day]?.sumOf { it.stepCount } ?: 0
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(day)
                val label = date?.let { SimpleDateFormat("EEE", Locale.US).format(it) } ?: day
                Pair(label, daySteps)
            }
        }
        "weekly" -> {
            val weeklyMap = mutableMapOf<Int, Int>()
            val cal = Calendar.getInstance()
            for ((day, windows) in dailyGroupsMap) {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(day)
                if (date != null) {
                    cal.time = date
                    val week = cal.get(Calendar.WEEK_OF_YEAR)
                    weeklyMap[week] = (weeklyMap[week] ?: 0) + windows.sumOf { it.stepCount }
                }
            }
            weeklyMap.entries.sortedBy { it.key }.takeLast(4).map { (week, steps) ->
                Pair("Week $week", steps)
            }
        }
        else -> {
            val monthlyMap = mutableMapOf<Int, Int>()
            val cal = Calendar.getInstance()
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            for ((day, windows) in dailyGroupsMap) {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(day)
                if (date != null) {
                    cal.time = date
                    val month = cal.get(Calendar.MONTH)
                    monthlyMap[month] = (monthlyMap[month] ?: 0) + windows.sumOf { it.stepCount }
                }
            }
            monthlyMap.entries.sortedBy { it.key }.takeLast(6).map { (month, steps) ->
                Pair(months.getOrElse(month) { "$month" }, steps)
            }
        }
    }

    val scoreValue = syncState?.inferenceResult?.score
    val confidence = syncState?.inferenceResult?.confidence
    val dailySteps = syncState?.dailySteps ?: 0
    val baseline = syncState?.baseline
    val baselineDays = syncState?.baselineDays ?: 0

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Overview",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = { loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(8.dp))

            // Main Risk Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                if (baseline == null && baselineDays < 5) {
                    BaselineProgress(
                        daysCollected = baselineDays.coerceAtMost(7),
                        daysRequired = 7
                    )
                } else {
                    ScoreCard(
                        score = scoreValue,
                        explanation = syncState?.inferenceResult?.explanation,
                        recommendation = syncState?.inferenceResult?.recommendation
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Activity Section
            Text(
                "Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) Color(0xFF0A84FF) else Color(0xFF007AFF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "$dailySteps",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Steps Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                GlassCard(modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (confidence != null) "${confidence.toInt()}%" else "--",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "AI Confidence",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Trends
            PeriodToggle(selected = trendView, onSelect = { trendView = it })

            Spacer(Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Movement History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                
                if (stepTrendData.isNotEmpty()) {
                    stepTrendData.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "$value",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    " steps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                } else {
                    Text(
                        "No trend data yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Stats Summary
            SummaryStats(avg = avgScore, best = bestScore, worst = worstScore)

            val currentResult = syncState?.inferenceResult
            if (currentResult?.deviations?.isNotEmpty() == true) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Clinical Insight",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    DeviationList(
                        deviations = currentResult.deviations.map { dev ->
                            com.example.gesturgaitai.model.Deviation(
                                feature = dev.feature,
                                deltaPercent = dev.deltaPercent,
                                direction = dev.direction
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
