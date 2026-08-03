package com.red.feature.profile

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ProfileInfo DTO with @JsonClass.
 */
class ProfileInfoTest {

  @Test
  fun `ProfileInfo has all fields`() {
    val info = ProfileInfo(
      fullName = "Test User",
      email = "test@example.com",
      phoneNumber = "+1234567890"
    )
    assertEquals("Test User", info.fullName)
    assertEquals("test@example.com", info.email)
    assertEquals("+1234567890", info.phoneNumber)
  }

  @Test
  fun `ProfileInfo defaults to empty strings`() {
    val info = ProfileInfo(fullName = "", email = "", phoneNumber = "")
    assertEquals("", info.fullName)
    assertEquals("", info.email)
    assertEquals("", info.phoneNumber)
  }

  @Test
  fun `ProfileInfo copy works correctly`() {
    val info = ProfileInfo(fullName = "Test", email = "t@e.com", phoneNumber = "123")
    val copied = info.copy(fullName = "Updated")
    assertEquals("Updated", copied.fullName)
    assertEquals("t@e.com", copied.email)
    assertEquals("123", copied.phoneNumber)
  }
}
