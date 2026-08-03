package com.red.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for the LogScrubber and security utilities.
 */
class SecurityTest {

  @Test
  fun `LogScrubber redacts IPv4 addresses`() {
    val input = "User connected from 192.168.1.50 at noon"
    val scrubbed = LogScrubber.scrub(input)
    assertFalse(scrubbed.contains("192.168.1.50"))
    assertTrue(scrubbed.contains("[ip]"))
  }

  @Test
  fun `LogScrubber redacts email addresses`() {
    val input = "Login attempt by admin@red.local failed"
    val scrubbed = LogScrubber.scrub(input)
    assertFalse(scrubbed.contains("admin@red.local"))
    assertTrue(scrubbed.contains("[email]"))
  }

  @Test
  fun `LogScrubber redacts Bearer tokens`() {
    val input = "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.abc.def"
    val scrubbed = LogScrubber.scrub(input)
    assertFalse(scrubbed.contains("eyJhbGciOiJIUzI1NiJ9"))
    assertTrue(scrubbed.contains("Bearer [redacted]"))
  }

  @Test
  fun `LogScrubber handles multiple redactions in one message`() {
    val input = "User admin@red.local from 10.0.0.1 used Bearer token123"
    val scrubbed = LogScrubber.scrub(input)
    assertFalse(scrubbed.contains("admin@red.local"))
    assertFalse(scrubbed.contains("10.0.0.1"))
    assertFalse(scrubbed.contains("token123"))
    assertTrue(scrubbed.contains("[email]"))
    assertTrue(scrubbed.contains("[ip]"))
    assertTrue(scrubbed.contains("Bearer [redacted]"))
  }

  @Test
  fun `LogScrubber leaves clean text untouched`() {
    val input = "System started successfully"
    assertEquals(input, LogScrubber.scrub(input))
  }
}
