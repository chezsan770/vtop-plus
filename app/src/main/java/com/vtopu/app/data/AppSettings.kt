package com.vtopu.app.data

import android.content.Context

class AppSettings(context: Context) {
    private val preferences = context.getSharedPreferences("vtop_u_settings", Context.MODE_PRIVATE)

    var backgroundKeepAliveEnabled: Boolean
        get() = preferences.getBoolean(backgroundKeepAliveKey, false)
        set(value) {
            preferences.edit().putBoolean(backgroundKeepAliveKey, value).apply()
        }

    var darkModeEnabled: Boolean
        get() = preferences.getBoolean(darkModeKey, true)
        set(value) {
            preferences.edit().putBoolean(darkModeKey, value).apply()
        }

    var appearanceTheme: String
        get() = preferences.getString(appearanceThemeKey, "classic") ?: "classic"
        set(value) {
            preferences.edit().putString(appearanceThemeKey, value).apply()
        }

    var hasSeenLanding: Boolean
        get() = preferences.getBoolean(hasSeenLandingKey, false)
        set(value) {
            preferences.edit().putBoolean(hasSeenLandingKey, value).apply()
        }

    var dismissedUpdateVersionCode: Int
        get() = preferences.getInt(dismissedUpdateVersionCodeKey, 0)
        set(value) {
            preferences.edit().putInt(dismissedUpdateVersionCodeKey, value).apply()
        }

    private companion object {
        const val backgroundKeepAliveKey = "background_keep_alive_enabled"
        const val darkModeKey = "dark_mode_enabled"
        const val appearanceThemeKey = "appearance_theme"
        const val hasSeenLandingKey = "has_seen_landing"
        const val dismissedUpdateVersionCodeKey = "dismissed_update_version_code"
    }
}
