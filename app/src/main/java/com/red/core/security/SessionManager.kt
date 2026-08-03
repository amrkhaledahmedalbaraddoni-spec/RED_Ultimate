package com.red.core.security

import android.content.Context
import android.content.SharedPreferences
import com.red.core.crypto.AESEncryption
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Session manager — handles secure storage of the auth token,
 * session keys, and app lock state.
 */
@Singleton
class SessionManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("red_session", Context.MODE_PRIVATE)
    }

    private var encryptionKey: SecretKey? = null

    /**
     * Get or create the local encryption key for message storage.
     */
    fun getEncryptionKey(): SecretKey {
        return encryptionKey ?: run {
            val stored = prefs.getString("enc_key", null)
            if (stored != null) {
                AESEncryption.keyFromBase64(stored)
            } else {
                val key = AESEncryption.generateKey()
                prefs.edit().putString("enc_key", AESEncryption.keyToBase64(key)).apply()
                key
            }.also { encryptionKey = it }
        }
    }

    /**
     * Encrypt a message for local storage.
     */
    fun encryptMessage(plaintext: String): String {
        return AESEncryption.encrypt(plaintext, getEncryptionKey())
    }

    /**
     * Decrypt a message from local storage.
     */
    fun decryptMessage(ciphertext: String): String {
        return AESEncryption.decrypt(ciphertext, getEncryptionKey())
    }

    /**
     * Clear all session data on logout.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
        encryptionKey = null
    }

    /**
     * Check if the session is valid (has a token).
     */
    fun hasSession(): Boolean {
        return !prefs.getString("auth_token", null).isNullOrBlank()
    }
}
