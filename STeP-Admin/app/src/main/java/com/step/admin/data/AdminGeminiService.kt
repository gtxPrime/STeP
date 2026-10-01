package com.step.admin.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AdminGeminiService {
    private const val TAG = "AdminGemini"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    var apiKey: String = com.step.admin.BuildConfig.GEMINI_API_KEY

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun draftDefectNotice(defectReason: String, studentName: String): String = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are the Ministry of Tribal Affairs (MoTA) Official Administrative AI.
            Draft a clear, polite, and actionable defect notice to the student explaining what needs to be rectified.
            Cite the MoTA scheme clause and explain step-by-step how to resolve it via DigiLocker or e-District.
            Keep it under 100 words.
        """.trimIndent()

        try {
            val url = "$BASE_URL?key=$apiKey"
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemPrompt\n\nStudent: $studentName\nBureaucratic Defect: $defectReason")
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = httpClient.newCall(request).execute()
            val respString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val root = JSONObject(respString)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val text = candidates.getJSONObject(0)
                        .optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")
                    if (!text.isNullOrBlank()) {
                        return@withContext text.trim()
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gemini draft error: ${e.message}")
        }

        return@withContext "Dear $studentName, your scholarship application requires a minor document update: $defectReason. Please upload the renewed certificate via your STeP Digital Wallet or re-sync with DigiLocker to expedite your sanction order."
    }
}
