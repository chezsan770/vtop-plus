package com.vtopu.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class SavedCredentials(
    val username: String,
    val password: String
)

class CredentialStore(context: Context) {
    private val preferences = context.getSharedPreferences("saved_credentials", Context.MODE_PRIVATE)

    fun load(): SavedCredentials? {
        val username = preferences.getString(usernameKey, null)?.takeIf { it.isNotBlank() } ?: return null
        val encryptedPassword = preferences.getString(passwordKey, null)?.takeIf { it.isNotBlank() } ?: return null
        val password = decrypt(encryptedPassword) ?: run {
            clear()
            return null
        }
        return SavedCredentials(username = username, password = password)
    }

    fun save(credentials: SavedCredentials) {
        val encryptedPassword = encrypt(credentials.password) ?: return
        preferences.edit()
            .putString(usernameKey, credentials.username.uppercase())
            .putString(passwordKey, encryptedPassword)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun encrypt(value: String): String? =
        runCatching {
            val cipher = Cipher.getInstance(cipherTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            "${cipher.iv.base64()}:${encrypted.base64()}"
        }.getOrNull()

    private fun decrypt(value: String): String? =
        runCatching {
            val parts = value.split(":")
            if (parts.size != 2) return@runCatching null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(cipherTransformation)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }.getOrNull()

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
        keyStore.getKey(keyAlias, null)?.let { key -> return key as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun ByteArray.base64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private companion object {
        const val androidKeyStore = "AndroidKeyStore"
        const val cipherTransformation = "AES/GCM/NoPadding"
        const val keyAlias = "vtop_u_saved_credentials"
        const val usernameKey = "username"
        const val passwordKey = "password"
    }
}
