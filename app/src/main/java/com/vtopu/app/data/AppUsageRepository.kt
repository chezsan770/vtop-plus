package com.vtopu.app.data

import com.vtopu.app.BuildConfig
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
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    suspend fun submitHeartbeat(heartbeat: ActiveUserHeartbeat): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase is not configured."))
        }

        runCatching {
            val endpoint = BuildConfig.SUPABASE_URL.trimEnd('/') +
                "/rest/v1/app_active_users?on_conflict=registration_number"
            val json = JSONObject()
                .put("registration_number", heartbeat.registrationNumber)
                .put("student_name", heartbeat.studentName)
                .put("app_version", heartbeat.appVersion)
                .put("last_seen", heartbeat.lastSeen)
                .toString()

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(json.toRequestBody("application/json".toMediaType()))
                .addLegacyAuthorizationIfNeeded()
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Supabase heartbeat failed with HTTP ${response.code}." }
            }
        }
    }

    private fun Request.Builder.addLegacyAuthorizationIfNeeded(): Request.Builder {
        if (!BuildConfig.SUPABASE_ANON_KEY.startsWith("sb_publishable_")) {
            addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        }
        return this
    }
}
