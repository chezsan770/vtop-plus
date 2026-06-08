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
import okhttp3.Cookie

class SessionCookieStore(context: Context) {
    private val preferences = context.getSharedPreferences("vtop_session_cookies", Context.MODE_PRIVATE)

    fun load(): List<Cookie> {
        val encryptedPayload = preferences.getString(cookiesKey, null)?.takeIf { it.isNotBlank() } ?: return emptyList()
        val payload = decrypt(encryptedPayload) ?: run {
            clear()
            return emptyList()
        }
        val now = System.currentTimeMillis()
        return payload
            .lineSequence()
            .mapNotNull(::decodeCookie)
            .filter { cookie -> cookie.expiresAt > now }
            .toList()
    }

    fun save(cookies: List<Cookie>) {
        val payload = cookies.joinToString("\n", transform = ::encodeCookie)
        val encryptedPayload = encrypt(payload) ?: return
        preferences.edit()
            .putString(cookiesKey, encryptedPayload)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun encodeCookie(cookie: Cookie): String =
        listOf(
            cookie.name.b64(),
            cookie.value.b64(),
            cookie.domain.b64(),
            cookie.path.b64(),
            cookie.expiresAt.toString(),
            cookie.secure.toString(),
            cookie.httpOnly.toString(),
            cookie.hostOnly.toString()
        ).joinToString("|")

    private fun decodeCookie(line: String): Cookie? =
        runCatching {
            val parts = line.split("|")
            if (parts.size != 8) return@runCatching null
            val builder = Cookie.Builder()
                .name(parts[0].fromB64())
                .value(parts[1].fromB64())
                .path(parts[3].fromB64())
                .expiresAt(parts[4].toLong())
            if (parts[7].toBoolean()) {
                builder.hostOnlyDomain(parts[2].fromB64())
            } else {
                builder.domain(parts[2].fromB64())
            }
            if (parts[5].toBoolean()) builder.secure()
            if (parts[6].toBoolean()) builder.httpOnly()
            builder.build()
        }.getOrNull()

    private fun encrypt(value: String): String? =
        runCatching {
            val cipher = Cipher.getInstance(cipherTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            "${cipher.iv.rawB64()}:${encrypted.rawB64()}"
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

    private fun String.b64(): String = toByteArray(Charsets.UTF_8).rawB64()

    private fun String.fromB64(): String = String(Base64.decode(this, Base64.NO_WRAP), Charsets.UTF_8)

    private fun ByteArray.rawB64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private companion object {
        const val androidKeyStore = "AndroidKeyStore"
        const val cipherTransformation = "AES/GCM/NoPadding"
        const val cookiesKey = "cookies"
        const val keyAlias = "vtop_u_session_cookies"
    }
}
