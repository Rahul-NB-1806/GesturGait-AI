package com.example.gesturgaitai.network

import com.example.gesturgaitai.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiClient {
    private val BASE_URL get() = NetworkConfig.BASE_URL

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

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

    suspend fun login(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("GesturGaitFeatures", "Attempting login for: $email at $BASE_URL")
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val request = Request.Builder()
                .url("$BASE_URL/auth/login")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            android.util.Log.d("GesturGaitFeatures", "Login Response Code: ${response.code}")
            
            if (body.isEmpty()) {
                android.util.Log.e("GesturGaitFeatures", "Server returned an empty body!")
                return@withContext AuthResponse(null, null, "Server returned empty response (Code: ${response.code})")
            }

            val obj = try {
                JSONObject(body)
            } catch (e: Exception) {
                android.util.Log.e("GesturGaitFeatures", "JSON Parse Error. Body was: $body")
                return@withContext AuthResponse(null, null, "Server returned invalid data")
            }
            
            if (response.isSuccessful) {
                val userJson = obj.getJSONObject("user")
                AuthResponse(
                    token = obj.getString("token"),
                    user = com.example.gesturgaitai.model.User(
                        _id = userJson.getString("_id"),
                        email = userJson.getString("email"),
                        patientId = userJson.optString("patientId", "")
                    ),
                    message = null
                )
            } else {
                val msg = obj.optString("message", "Login failed")
                android.util.Log.e("GesturGaitFeatures", "Login failed: $msg")
                AuthResponse(null, null, msg)
            }
        } catch (e: Exception) {
            android.util.Log.e("GesturGaitFeatures", "Login connection error: ${e.message}")
            AuthResponse(null, null, "Connection error: ${e.message}")
        }
    }

    suspend fun register(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("GesturGaitFeatures", "Attempting registration for: $email at $BASE_URL")
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val request = Request.Builder()
                .url("$BASE_URL/auth/register")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            android.util.Log.d("GesturGaitFeatures", "Register Response Code: ${response.code}")
            
            if (body.isEmpty()) {
                android.util.Log.e("GesturGaitFeatures", "Server returned an empty body!")
                return@withContext AuthResponse(null, null, "Server returned empty response (Code: ${response.code})")
            }

            val obj = try {
                JSONObject(body)
            } catch (e: Exception) {
                android.util.Log.e("GesturGaitFeatures", "JSON Parse Error. Body was: $body")
                return@withContext AuthResponse(null, null, "Server returned invalid data")
            }
            
            if (response.isSuccessful) {
                val userJson = obj.getJSONObject("user")
                AuthResponse(
                    token = obj.getString("token"),
                    user = com.example.gesturgaitai.model.User(
                        _id = userJson.getString("_id"),
                        email = userJson.getString("email"),
                        patientId = userJson.optString("patientId", "")
                    ),
                    message = null
                )
            } else {
                val msg = obj.optString("message", "Registration failed")
                android.util.Log.e("GesturGaitFeatures", "Registration failed: $msg")
                AuthResponse(null, null, msg)
            }
        } catch (e: Exception) {
            android.util.Log.e("GesturGaitFeatures", "Register connection error: ${e.message}")
            AuthResponse(null, null, "Connection error: ${e.message}")
        }
    }

    suspend fun syncScore(
        userId: String,
        date: String,
        score: Int,
        confidence: Double,
        stepCount: Int,
        features: Map<String, Double>,
        deviations: List<com.example.gesturgaitai.model.Deviation>?,
        explanation: String?,
        recommendation: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("userId", userId)
                put("date", date)
                put("score", score)
                put("confidence", confidence)
                put("stepCount", stepCount)
                put("features", JSONObject().apply {
                    features.forEach { (k, v) -> put(k, v) }
                })
                deviations?.let { devList ->
                    put("deviations", org.json.JSONArray().apply {
                        devList.forEach { dev ->
                            put(JSONObject().apply {
                                put("feature", dev.feature)
                                put("deltaPercent", dev.deltaPercent)
                                put("direction", dev.direction)
                            })
                        }
                    })
                }
                put("explanation", explanation)
                put("recommendation", recommendation)
            }

            val request = Request.Builder()
                .url("$BASE_URL/score/sync")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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
