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
        .connectTimeout(60, TimeUnit.SECONDS) // Wait 60s for Render to wake up
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private var authToken: String? = null

    fun setToken(token: String?) {
        authToken = token
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
            
            android.util.Log.d("GesturGaitFeatures", "Requesting: ${request.url}")
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
                    user = User(
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
                
            android.util.Log.d("GesturGaitFeatures", "Requesting: ${request.url}")
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            android.util.Log.d("GesturGaitFeatures", "Register Response Code: ${response.code}")
            
            if (body.isEmpty()) {
                android.util.Log.e("GesturGaitFeatures", "Server returned an empty body!")
                return@withContext AuthResponse(null, null, "Server returned empty response (Code: ${response.code})")
            }

            val obj = try {
                JSONObject(body)
            } catch (jsone: Exception) {
                android.util.Log.e("GesturGaitFeatures", "JSON Parse Error. Body was: $body")
                return@withContext AuthResponse(null, null, "Server returned invalid data")
            }
            
            if (response.isSuccessful) {
                val userJson = obj.getJSONObject("user")
                AuthResponse(
                    token = obj.getString("token"),
                    user = User(
                        _id = userJson.getString("_id"),
                        email = userJson.getString("email"),
                        patientId = userJson.optString("patientId", "")
                    ),
                    message = null
                )
            } else {
                // Return the actual error message from the server (e.g. "Email already registered")
                val msg = obj.optString("error", obj.optString("message", "Registration failed"))
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
}
