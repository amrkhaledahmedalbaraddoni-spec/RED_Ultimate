package com.red.core.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM encryption for local message storage.
 * Used for encrypting messages at rest on the device.
 *
 * Note: This is NOT end-to-end encryption. E2E would require
 * the Signal Protocol (X3DH + Double Ratchet) for true security.
 * This is a local encryption layer for the Room database.
 */
object AESEncryption {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    /**
     * Generate a new AES-256 key.
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(KEY_SIZE, SecureRandom())
        return keyGenerator.generateKey()
    }

    /**
     * Encrypt plaintext using AES-256-GCM.
     * @return Base64-encoded string: IV + ciphertext
     */
    fun encrypt(plaintext: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        // Prepend IV to ciphertext
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypt Base64-encoded ciphertext using AES-256-GCM.
     */
    fun decrypt(encodedCiphertext: String, key: SecretKey): String {
        val combined = Base64.decode(encodedCiphertext, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    /**
     * Serialize a key to Base64 for storage.
     */
    fun keyToBase64(key: SecretKey): String {
        return Base64.encodeToString(key.encoded, Base64.NO_WRAP)
    }

    /**
     * Deserialize a key from Base64.
     */
    fun keyFromBase64(encoded: String): SecretKey {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        return SecretKeySpec(bytes, "AES")
    }
}
