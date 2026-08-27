package com.example.gesturgaitai.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AccessibilityDataUploader {

    private const val BASE_URL = "http://10.0.2.2:3000"
    private val JSON = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private var authToken: String? = null

    fun setToken(token: String?) {
        authToken = token
    }

    suspend fun uploadWindow(
        userId: String,
        window: AccessibilityFeatureWindow
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("windows", JSONArray().put(
                    JSONObject().apply {
                        put("userId", userId)
                        put("timestamp", System.currentTimeMillis())
                        put("windowDurationSec", 5)
                        put("source", "accessibility")
                        put("features", JSONObject().apply {
                            put("swipeSpeed", window.swipeSpeed)
                            put("tapInterval", window.tapInterval)
                            put("gestureDurationVariance", window.gestureDurationVariance)
                            put("tapCount", window.tapCount)
                            put("scrollDistance", window.scrollDistance)
                        })
                    }
                ))
            }

            val request = Request.Builder()
                .url("$BASE_URL/features")
                .addHeader("Content-Type", "application/json")
                .apply { authToken?.let { addHeader("Authorization", "Bearer $it") } }
                .post(json.toString().toRequestBody(JSON))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
