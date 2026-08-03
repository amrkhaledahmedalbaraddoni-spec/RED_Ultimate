package com.red.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for InputValidator.
 */
class InputValidatorTest {

    private val validator = InputValidator()

    // ── Name validation ──────────────────────────────────────────────────────

    @Test
    fun `valid name passes`() {
        val result = validator.validateName("Ahmed Al-Baraddoni")
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `blank name fails`() {
        val result = validator.validateName("")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("blank") })
    }

    @Test
    fun `name with XSS script tag fails`() {
        val result = validator.validateName("<script>alert('xss')</script>")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("invalid") })
    }

    // ── Email validation ─────────────────────────────────────────────────────

    @Test
    fun `valid email passes`() {
        val result = validator.validateEmail("test@example.com")
        assertTrue(result.isValid)
    }

    @Test
    fun `invalid email fails`() {
        val result = validator.validateEmail("not-an-email")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("format") })
    }

    @Test
    fun `blank email fails`() {
        val result = validator.validateEmail("")
        assertFalse(result.isValid)
    }

    // ── Phone validation ─────────────────────────────────────────────────────

    @Test
    fun `valid phone passes`() {
        val result = validator.validatePhone("+967123456789")
        assertTrue(result.isValid)
    }

    @Test
    fun `invalid phone fails`() {
        val result = validator.validatePhone("abc123")
        assertFalse(result.isValid)
    }

    // ── Password validation ──────────────────────────────────────────────────

    @Test
    fun `strong password passes`() {
        val result = validator.validatePassword("Str0ngP@ss!")
        assertTrue(result.isValid)
    }

    @Test
    fun `short password fails`() {
        val result = validator.validatePassword("Ab1")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("8") })
    }

    @Test
    fun `password without digit fails`() {
        val result = validator.validatePassword("Abcdefgh")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("digit") })
    }

    @Test
    fun `password without uppercase fails`() {
        val result = validator.validatePassword("abcdefgh1")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("uppercase") })
    }

    // ── Message validation ───────────────────────────────────────────────────

    @Test
    fun `valid message passes`() {
        val result = validator.validateMessagePayload("Hello, world!")
        assertTrue(result.isValid)
    }

    @Test
    fun `blank message fails`() {
        val result = validator.validateMessagePayload("")
        assertFalse(result.isValid)
    }

    // ── Group name validation ────────────────────────────────────────────────

    @Test
    fun `valid group name passes`() {
        val result = validator.validateGroupName("RED Team")
        assertTrue(result.isValid)
    }

    @Test
    fun `blank group name fails`() {
        val result = validator.validateGroupName("")
        assertFalse(result.isValid)
    }

    // ── Sanitization ─────────────────────────────────────────────────────────

    @Test
    fun `sanitize escapes HTML`() {
        val result = validator.sanitize("<script>alert(1)</script>")
        assertFalse(result.contains("<script>"))
        assertTrue(result.contains("&lt;script&gt;"))
    }

    @Test
    fun `sanitize trims whitespace`() {
        val result = validator.sanitize("  hello  ")
        assertEquals("hello", result)
    }
}
