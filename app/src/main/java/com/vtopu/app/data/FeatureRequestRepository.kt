package com.vtopu.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class FeatureRequestRepository {
    private val client = OkHttpClient()

    val isConfigured: Boolean
        get() = SupabaseConfig.isConfigured

    suspend fun submitFeatureRequest(payload: FeatureRequestPayload): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase is not configured."))
        }

        runCatching {
            val endpoint = SupabaseConfig.baseUrl + "/rest/v1/feature_requests"
            val json = JSONObject()
                .put("title", payload.title)
                .put("description", payload.description)
                .put("category", payload.category)
                .put("student_name", payload.studentName)
                .put("registration_number", payload.registrationNumber)
                .put("app_version", payload.appVersion)
                .put("created_at", payload.createdAt)
                .toString()

            val request = Request.Builder()
                .url(endpoint)
                .addSupabaseHeaders()
                .addHeader("Prefer", "return=minimal")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Supabase request failed with HTTP ${response.code}." }
            }
        }
    }
}
