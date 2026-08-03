package com.red.core.crypto

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AES-256-GCM encryption.
 */
class AESEncryptionTest {

    @Test
    fun `encrypt and decrypt should return original plaintext`() {
        val key = AESEncryption.generateKey()
        val plaintext = "Hello, RED! This is a test message."
        
        val ciphertext = AESEncryption.encrypt(plaintext, key)
        val decrypted = AESEncryption.decrypt(ciphertext, key)
        
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `different encryptions should produce different ciphertexts`() {
        val key = AESEncryption.generateKey()
        val plaintext = "Same message"
        
        val cipher1 = AESEncryption.encrypt(plaintext, key)
        val cipher2 = AESEncryption.encrypt(plaintext, key)
        
        // Different IVs should produce different ciphertexts
        assertNotEquals(cipher1, cipher2)
    }

    @Test
    fun `key serialization round-trip should work`() {
        val key = AESEncryption.generateKey()
        val encoded = AESEncryption.keyToBase64(key)
        val decoded = AESEncryption.keyFromBase64(encoded)
        
        val plaintext = "Test with serialized key"
        val ciphertext = AESEncryption.encrypt(plaintext, decoded)
        val decrypted = AESEncryption.decrypt(ciphertext, decoded)
        
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encryption should handle empty string`() {
        val key = AESEncryption.generateKey()
        val plaintext = ""
        
        val ciphertext = AESEncryption.encrypt(plaintext, key)
        val decrypted = AESEncryption.decrypt(ciphertext, key)
        
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encryption should handle unicode`() {
        val key = AESEncryption.generateKey()
        val plaintext = "مرحبا بالعالم 🌍 Hello мир"
        
        val ciphertext = AESEncryption.encrypt(plaintext, key)
        val decrypted = AESEncryption.decrypt(ciphertext, key)
        
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encryption should handle long text`() {
        val key = AESEncryption.generateKey()
        val plaintext = "A".repeat(10000)
        
        val ciphertext = AESEncryption.encrypt(plaintext, key)
        val decrypted = AESEncryption.decrypt(ciphertext, key)
        
        assertEquals(plaintext, decrypted)
    }
}
