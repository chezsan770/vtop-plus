package com.vtopu.app.data

import com.vtopu.app.BuildConfig
import okhttp3.Request

internal object SupabaseConfig {
    val baseUrl: String
        get() = BuildConfig.SUPABASE_URL.trimEnd('/')

    val anonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && anonKey.isNotBlank()
}

internal fun Request.Builder.addSupabaseHeaders(): Request.Builder {
    addHeader("apikey", SupabaseConfig.anonKey)
    if (!SupabaseConfig.anonKey.startsWith("sb_publishable_")) {
        addHeader("Authorization", "Bearer ${SupabaseConfig.anonKey}")
    }
    return this
}
