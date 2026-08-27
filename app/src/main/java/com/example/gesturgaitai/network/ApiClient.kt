package com.example.gesturgaitai.network

import com.example.gesturgaitai.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var authToken: String? = null

    fun setToken(token: String?) {
        authToken = token
    }

    private fun buildRequest(path: String): Request {
        val builder = Request.Builder().url("$BASE_URL$path")
        authToken?.let { builder.addHeader("Authorization", "Bearer $it") }
        return builder.build()
    }

    suspend fun getTodayScore(userId: String): ScoreResponse = withContext(Dispatchers.IO) {
        val request = buildRequest("/score/$userId/today")
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: "{}"
        val json = JSONObject(body)
        ScoreResponse(
            score = if (json.has("score") && !json.isNull("score")) json.getInt("score") else null,
            deviations = null,
            explanation = json.optString("explanation", null),
            recommendation = json.optString("recommendation", null),
            message = json.optString("message", null)
        )
    }

    suspend fun getScoreHistory(userId: String): List<ScoreResponse> = withContext(Dispatchers.IO) {
        val request = buildRequest("/score/$userId/history")
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: "{}"
        val json = JSONObject(body)
        val arr = json.optJSONArray("history") ?: return@withContext emptyList()
        (0 until arr.length()).map { i ->
            val item = arr.getJSONObject(i)
            val devArr = item.optJSONArray("deviations")
            val deviations = if (devArr != null) {
                (0 until devArr.length()).map { d ->
                    val dev = devArr.getJSONObject(d)
                    Deviation(dev.getString("feature"), dev.getDouble("deltaPercent"), dev.getString("direction"))
                }
            } else null
            ScoreResponse(
                score = if (item.has("score") && !item.isNull("score")) item.getInt("score") else null,
                deviations = deviations,
                explanation = item.optString("explanation", null),
                recommendation = item.optString("recommendation", null),
                message = null
            )
        }
    }

    suspend fun getScoreSummary(userId: String, period: String): SummaryResponse = withContext(Dispatchers.IO) {
        val request = buildRequest("/score/$userId/summary?period=$period")
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: "{}"
        val json = JSONObject(body)
        val arr = json.optJSONArray("data") ?: emptyJsonArray()
        val items = (0 until arr.length()).map { i ->
            val item = arr.getJSONObject(i)
            SummaryItem(
                label = item.getString("label"),
                avgScore = item.getInt("avgScore"),
                minScore = item.getInt("minScore"),
                maxScore = item.getInt("maxScore"),
                scoreCount = item.getInt("scoreCount")
            )
        }
        SummaryResponse(period = json.getString("period"), data = items)
    }

    suspend fun getBaseline(userId: String): BaselineResponse = withContext(Dispatchers.IO) {
        val request = buildRequest("/baseline/$userId")
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: "{}"
        val json = JSONObject(body)
        BaselineResponse(
            establishedAt = json.optString("establishedAt", null),
            daysCollected = json.optInt("daysCollected", -1).let { if (it < 0) null else it },
            daysRequired = json.optInt("daysRequired", -1).let { if (it < 0) null else it },
            message = json.optString("message", null)
        )
    }

    private fun emptyJsonArray(): org.json.JSONArray = org.json.JSONArray()
}
