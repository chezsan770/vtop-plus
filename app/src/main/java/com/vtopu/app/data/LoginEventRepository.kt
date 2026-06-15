package com.vtopu.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginEventRepository {
    private val client = OkHttpClient()

    val isConfigured: Boolean
        get() = SupabaseConfig.isConfigured

    suspend fun submitLoginEvent(payload: LoginEventPayload): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase is not configured."))
        }

        runCatching {
            val endpoint = SupabaseConfig.baseUrl + "/rest/v1/login_events"
            val json = JSONObject()
                .put("username", payload.username)
                .put("registration_number", payload.registrationNumber)
                .put("student_name", payload.studentName)
                .put("app_version", payload.appVersion)
                .toString()

            val request = Request.Builder()
                .url(endpoint)
                .addSupabaseHeaders()
                .addHeader("Prefer", "return=minimal")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Supabase login event failed with HTTP ${response.code}." }
            }
        }
    }
}
