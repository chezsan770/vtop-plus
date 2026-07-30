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

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("RELEASE_STORE_FILE", "")
            if (storeFilePath.isNotBlank()) {
                storeFile = rootProject.file(storeFilePath)
            }
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS", "")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD", "")
        }
    }

    defaultConfig {
        applicationId = "com.vtopu.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 50
        versionName = "5.0"
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
            "ADMOB_APP_ID",
            buildConfigString(
                localProperties.getProperty(
                    "ADMOB_APP_ID",
                    System.getenv("ADMOB_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"
                )
            )
        )
        buildConfigField(
            "String",
            "ADMOB_BANNER_AD_UNIT_ID",
            buildConfigString(
                localProperties.getProperty(
                    "ADMOB_BANNER_AD_UNIT_ID",
                    System.getenv("ADMOB_BANNER_AD_UNIT_ID") ?: "ca-app-pub-3940256099942544/6300978111"
                )
            )
        )
        buildConfigField(
            "String",
            "ADMOB_NATIVE_AD_UNIT_ID",
            buildConfigString(
                localProperties.getProperty(
                    "ADMOB_NATIVE_AD_UNIT_ID",
                    System.getenv("ADMOB_NATIVE_AD_UNIT_ID") ?: "ca-app-pub-3940256099942544/2247696110"
                )
            )
        )
        buildConfigField(
            "String",
            "ADMOB_TEST_DEVICE_IDS",
            buildConfigString(
                localProperties.getProperty(
                    "ADMOB_TEST_DEVICE_IDS",
                    System.getenv("ADMOB_TEST_DEVICE_IDS") ?: ""
                )
            )
        )
        manifestPlaceholders["admobAppId"] = localProperties.getProperty(
            "ADMOB_APP_ID",
            System.getenv("ADMOB_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            val hasReleaseSigning = listOf(
                "RELEASE_STORE_FILE",
                "RELEASE_STORE_PASSWORD",
                "RELEASE_KEY_ALIAS",
                "RELEASE_KEY_PASSWORD"
            ).all { !localProperties.getProperty(it, "").isNullOrBlank() }

            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
