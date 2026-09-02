package com.example.gesturgaitai.core

import android.content.Context
import android.util.Log
import com.example.gesturgaitai.network.ApiClient
import org.json.JSONArray
import org.json.JSONObject

object OfflineStorage {

    private const val TAG = "GesturGaitFeatures"
    private const val WINDOWS_FILE = "feature_windows.json"
    private const val BASELINE_FILE = "baseline.json"
    private const val SCORES_FILE = "scores.json"
    private const val DAILY_INDEX_FILE = "daily_index.json"
    private const val USER_PREFS = "user_prefs.json"

    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx
        // Restore auth token on init
        getAuthToken()?.let { ApiClient.setToken(it) }
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    fun getAuthToken(): String? {
        val file = getFile(USER_PREFS) ?: return null
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            val token = json.optString("authToken", "")
            if (token.isEmpty()) null else token
        } catch (e: Exception) {
            null
        }
    }

    fun saveAuth(email: String, token: String, patientId: String) {
        val file = getFile(USER_PREFS) ?: return
        val json = if (file.exists()) JSONObject(file.readText()) else JSONObject()
        json.put("email", email)
        json.put("authToken", token)
        json.put("patientId", patientId)
        file.writeText(json.toString())
        ApiClient.setToken(token)
    }

    fun logout() {
        val file = getFile(USER_PREFS) ?: return
        if (file.exists()) {
            val json = JSONObject(file.readText())
            json.remove("authToken")
            file.writeText(json.toString())
        }
        ApiClient.setToken(null)
    }

    fun getPatientId(): String? {
        val file = getFile(USER_PREFS) ?: return null
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            val id = json.optString("patientId", "")
            if (id.isEmpty()) null else id
        } catch (e: Exception) {
            null
        }
    }

    fun setPatientId(id: String) {
        val file = getFile(USER_PREFS) ?: return
        val json = if (file.exists()) JSONObject(file.readText()) else JSONObject()
        json.put("patientId", id)
        file.writeText(json.toString())
    }

    fun logHealthStatus() {
        val windows = getWindows()
        val scores = getScores()
        val hasBaseline = hasBaseline()
        
        Log.i(TAG, "========== GESTURGAIT MONITORING STATUS ==========")
        Log.i(TAG, "Accessibility Service: ${if (com.example.gesturgaitai.service.MotorAccessibilityService.isRunning) "RUNNING" else "STOPPED"}")
        Log.i(TAG, "Baseline Established: ${if (hasBaseline) "YES" else "NO"}")
        Log.i(TAG, "Feature Windows Stored: ${windows.size}")
        Log.i(TAG, "Recent Scores Stored: ${scores.size}")
        if (windows.isNotEmpty()) {
            val last = windows.last()
            Log.i(TAG, "Last Feature Window: ts=${last.timestamp}, source=${last.source}")
        }
        Log.i(TAG, "===================================================")
    }

    private fun getFile(name: String) =
        context?.let { java.io.File(it.filesDir, name) }

    // === FEATURE WINDOWS ===

    fun saveWindow(window: UnifiedFeatureWindow) {
        val file = getFile(WINDOWS_FILE) ?: return
        val arr = loadJsonArray(file)
        arr.put(windowToJson(window))
        trimToMax(arr, 10000)
        file.writeText(arr.toString())
    }

    fun saveWindows(windows: List<UnifiedFeatureWindow>) {
        val file = getFile(WINDOWS_FILE) ?: return
        val arr = JSONArray()
        val timestamp = System.currentTimeMillis()
        for ((i, w) in windows.withIndex()) {
            arr.put(windowToJson(w.copy(timestamp = timestamp + i)))
        }
        file.writeText(arr.toString())
    }

    fun getWindows(): List<UnifiedFeatureWindow> {
        val file = getFile(WINDOWS_FILE) ?: return emptyList()
        val arr = loadJsonArray(file)
        return (0 until arr.length()).map { jsonToWindow(arr.getJSONObject(it)) }
    }

    fun getWindowsSince(timestamp: Long): List<UnifiedFeatureWindow> {
        return getWindows().filter { it.timestamp >= timestamp }
    }

    fun getWindowsGroupedByDay(): Map<String, List<UnifiedFeatureWindow>> {
        return getWindows().groupBy { w ->
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            sdf.format(java.util.Date(w.timestamp))
        }
    }

    // === BASELINE ===

    fun saveBaseline(baseline: Baseline) {
        val file = getFile(BASELINE_FILE) ?: return
        val json = JSONObject().apply {
            put("means", mapToJson(baseline.means))
            put("stdDevs", mapToJson(baseline.stdDevs))
            put("emaValues", mapToJson(baseline.emaValues))
            put("sampleCount", baseline.sampleCount)
            put("establishedAt", baseline.establishedAt)
        }
        file.writeText(json.toString())
    }

    fun loadBaseline(): Baseline? {
        val file = getFile(BASELINE_FILE) ?: return null
        if (!file.exists()) return null
        val json = JSONObject(file.readText())
        return Baseline(
            means = jsonToMap(json.optJSONObject("means")),
            stdDevs = jsonToMap(json.optJSONObject("stdDevs")),
            emaValues = jsonToMap(json.optJSONObject("emaValues")),
            sampleCount = json.optInt("sampleCount"),
            establishedAt = json.optLong("establishedAt")
        )
    }

    fun hasBaseline(): Boolean {
        return getFile(BASELINE_FILE)?.exists() == true
    }

    // === SCORES ===

    data class StoredScore(
        val score: Int,
        val confidence: Double,
        val explanation: String,
        val recommendation: String,
        val timestamp: Long
    )

    fun saveScore(sd: StoredScore) {
        val file = getFile(SCORES_FILE) ?: return
        val arr = loadJsonArray(file)
        val json = JSONObject().apply {
            put("score", sd.score)
            put("confidence", sd.confidence)
            put("explanation", sd.explanation)
            put("recommendation", sd.recommendation)
            put("timestamp", sd.timestamp)
        }
        arr.put(json)
        trimToMax(arr, 365)
        file.writeText(arr.toString())
    }

    fun getScores(): List<StoredScore> {
        val file = getFile(SCORES_FILE) ?: return emptyList()
        if (!file.exists()) return emptyList()
        val arr = loadJsonArray(file)
        return (0 until arr.length()).map { i ->
            val json = arr.getJSONObject(i)
            StoredScore(
                score = json.getInt("score"),
                confidence = json.getDouble("confidence"),
                explanation = json.optString("explanation", ""),
                recommendation = json.optString("recommendation", ""),
                timestamp = json.getLong("timestamp")
            )
        }.sortedByDescending { it.timestamp }
    }

    fun getScoresForLastDays(days: Int): List<StoredScore> {
        val cutoff = System.currentTimeMillis() - days * 86400000L
        return getScores().filter { it.timestamp >= cutoff }
    }

    // === DAILY INDEX ===

    fun saveDailyIndex(dateStr: String, count: Int) {
        val file = getFile(DAILY_INDEX_FILE) ?: return
        val json = if (file.exists()) JSONObject(file.readText()) else JSONObject()
        json.put(dateStr, count)
        file.writeText(json.toString())
    }

    fun getDailyCounts(): Map<String, Int> {
        val file = getFile(DAILY_INDEX_FILE) ?: return emptyMap()
        if (!file.exists()) return emptyMap()
        val json = JSONObject(file.readText())
        val result = mutableMapOf<String, Int>()
        for (key in json.keys()) {
            result[key] = json.getInt(key)
        }
        return result
    }

    // === HELPERS ===

    private fun windowToJson(w: UnifiedFeatureWindow) = JSONObject().apply {
        put("timestamp", w.timestamp)
        put("stepCount", w.stepCount)
        put("avgStepTime", w.avgStepTime)
        put("peakFrequency", w.peakFrequency)
        put("tremorFrequency", w.tremorFrequency)
        put("movementStability", w.movementStability)
        put("swipeSpeed", w.swipeSpeed)
        put("tapInterval", w.tapInterval)
        put("gestureDurationVariance", w.gestureDurationVariance)
        put("tapCount", w.tapCount)
        put("scrollDistance", w.scrollDistance)
        put("source", w.source)
    }

    private fun jsonToWindow(json: JSONObject) = UnifiedFeatureWindow(
        timestamp = json.getLong("timestamp"),
        stepCount = json.optInt("stepCount"),
        avgStepTime = json.optDouble("avgStepTime"),
        peakFrequency = json.optDouble("peakFrequency"),
        tremorFrequency = json.optDouble("tremorFrequency"),
        movementStability = json.optDouble("movementStability"),
        swipeSpeed = json.optDouble("swipeSpeed"),
        tapInterval = json.optDouble("tapInterval"),
        gestureDurationVariance = json.optDouble("gestureDurationVariance"),
        tapCount = json.optInt("tapCount"),
        scrollDistance = json.optDouble("scrollDistance"),
        source = json.optString("source")
    )

    private fun loadJsonArray(file: java.io.File): JSONArray {
        if (!file.exists()) return JSONArray()
        return try { JSONArray(file.readText()) } catch (_: Exception) { JSONArray() }
    }

    private fun trimToMax(arr: JSONArray, max: Int) {
        while (arr.length() > max) arr.remove(0)
    }

    private fun mapToJson(map: Map<String, Double>): JSONObject {
        val json = JSONObject()
        for ((k, v) in map) json.put(k, v)
        return json
    }

    private fun jsonToMap(json: JSONObject?): Map<String, Double> {
        if (json == null) return emptyMap()
        val result = mutableMapOf<String, Double>()
        for (key in json.keys()) {
            result[key] = json.getDouble(key)
        }
        return result
    }
}
