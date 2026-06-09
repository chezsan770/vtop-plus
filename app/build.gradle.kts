import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { input ->
            load(input)
        }
    }
}

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.vtopu.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vtopu.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 44
        versionName = "4.4"
        buildConfigField(
            "String",
            "SUPABASE_URL",
            buildConfigString(localProperties.getProperty("SUPABASE_URL", ""))
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            buildConfigString(localProperties.getProperty("SUPABASE_ANON_KEY", ""))
        )
        buildConfigField(
            "String",
            "STARTAPP_APP_ID",
            buildConfigString(
                localProperties.getProperty(
                    "STARTAPP_APP_ID",
                    System.getenv("STARTAPP_APP_ID") ?: ""
                )
            )
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.startio.inapp.sdk)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
