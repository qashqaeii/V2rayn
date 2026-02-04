package com.v2ray.ang.api

import android.util.Base64
import android.util.Log
import com.v2ray.ang.AppConfig
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Decrypts VLESS/config payload encrypted by backend (AES-256-CBC).
 * Format: Base64( IV_16_bytes + ciphertext ). PKCS7 padding.
 * Key must match backend CONFIG_ENCRYPTION_KEY (32 bytes).
 */
object ConfigDecryptor {

    private const val IV_LENGTH = 16
    private const val KEY_LENGTH = 32
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"

    /**
     * Decrypts base64-encoded payload (IV + ciphertext) with the given key.
     * @param base64Payload Base64 string from API config field
     * @param keyUtf8 Key as UTF-8 string (will be truncated/padded to 32 bytes)
     * @return Decrypted plaintext (e.g. VLESS URL) or null on failure
     */
    fun decrypt(base64Payload: String?, keyUtf8: String?): String? {
        if (base64Payload.isNullOrBlank() || keyUtf8.isNullOrBlank()) return null
        val keyBytes = keyUtf8.encodeToByteArray().copyOf(KEY_LENGTH)
        if (keyBytes.size != KEY_LENGTH) return null
        return try {
            val raw = Base64.decode(base64Payload, Base64.NO_WRAP)
            if (raw.size < IV_LENGTH) return null
            val iv = raw.copyOfRange(0, IV_LENGTH)
            val ciphertext = raw.copyOfRange(IV_LENGTH, raw.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, ALGORITHM), IvParameterSpec(iv))
            val decrypted = cipher.doFinal(ciphertext)
            decrypted.decodeToString()
        } catch (e: Exception) {
            Log.w(AppConfig.TAG, "Config decrypt failed", e)
            null
        }
    }
}
