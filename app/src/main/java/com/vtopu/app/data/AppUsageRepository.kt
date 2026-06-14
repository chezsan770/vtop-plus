package com.vtopu.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AppUsageRepository {
    private val client = OkHttpClient()

    val isConfigured: Boolean
        get() = SupabaseConfig.isConfigured

    suspend fun submitHeartbeat(heartbeat: ActiveUserHeartbeat): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase is not configured."))
        }

        runCatching {
            val endpoint = SupabaseConfig.baseUrl + "/rest/v1/rpc/track_app_heartbeat"
            val json = JSONObject()
                .put("p_registration_number", heartbeat.registrationNumber)
                .put("p_student_name", heartbeat.studentName)
                .put("p_app_version", heartbeat.appVersion)
                .toString()

            val request = Request.Builder()
                .url(endpoint)
                .addSupabaseHeaders()
                .addHeader("Prefer", "return=minimal")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Supabase heartbeat failed with HTTP ${response.code}." }
            }
        }
    }
}
