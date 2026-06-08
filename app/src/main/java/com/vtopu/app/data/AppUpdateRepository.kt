package com.vtopu.app.data

import com.vtopu.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class AppUpdateRepository {
    private val client = OkHttpClient()

    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    suspend fun fetchLatestUpdate(): Result<AppUpdate?> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.success(null)
        }

        runCatching {
            val endpoint = BuildConfig.SUPABASE_URL.trimEnd('/') +
                "/rest/v1/app_updates?is_active=eq.true&order=version_code.desc&limit=1"

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .get()
                .addLegacyAuthorizationIfNeeded()
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Supabase update check failed with HTTP ${response.code}." }
                val body = response.body?.string().orEmpty()
                val latest = JSONArray(body).optJSONObject(0) ?: return@use null

                AppUpdate(
                    versionName = latest.optString("version_name"),
                    versionCode = latest.optInt("version_code"),
                    title = latest.optString("title", "Update available"),
                    message = latest.optString("message", "A new version of Gamma is available."),
                    apkUrl = latest.optString("apk_url").takeIf { it.isNotBlank() && it != "null" },
                    changelogUrl = latest.optString("changelog_url").takeIf { it.isNotBlank() && it != "null" },
                    isForceUpdate = latest.optBoolean("is_force_update", false)
                )
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
