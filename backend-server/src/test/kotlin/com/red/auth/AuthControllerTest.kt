package com.red.auth

import com.red.config.JwtService
import com.red.config.JwtProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
 * Unit tests for the auth flow: registration, login, approval, and JWT generation.
 */
class AuthControllerTest {

  private val passwordEncoder = BCryptPasswordEncoder(12)
  private val jwtService = JwtService(JwtProperties().apply {
    secret = "test-secret-key-that-is-at-least-32-bytes-long-for-hs256"
  })

  @Test
  fun `password encoding and matching works`() {
    val raw = "testPassword123"
    val encoded = passwordEncoder.encode(raw)
    assertNotEquals(raw, encoded)
    assertTrue(passwordEncoder.matches(raw, encoded))
    assertFalse(passwordEncoder.matches("wrongPassword", encoded))
  }

  @Test
  fun `JWT token generation and parsing works`() {
    val token = jwtService.generateToken("user-123", "USER")
    assertNotNull(token)
    val claims = jwtService.parse(token)
    assertNotNull(claims)
    assertEquals("user-123", claims!!.subject)
    assertEquals("USER", claims.get("role", String::class.java))
  }

  @Test
  fun `JWT token with invalid signature returns null`() {
    val wrongJwtService = JwtService(JwtProperties().apply {
      secret = "this-is-a-different-secret-key-with-32-bytes"
    })
    val token = jwtService.generateToken("user-123", "USER")
    val parsed = wrongJwtService.parse(token)
    assertNull(parsed)
  }

  @Test
  fun `UserEntity equality is based on id`() {
    val user1 = UserEntity(id = "abc", email = "a@test.com", passwordHash = "x", fullName = "A")
    val user2 = UserEntity(id = "abc", email = "b@test.com", passwordHash = "y", fullName = "B")
    val user3 = UserEntity(id = "xyz", email = "a@test.com", passwordHash = "x", fullName = "A")
    assertEquals(user1, user2)
    assertNotEquals(user1, user3)
  }

  @Test
  fun `UserView from entity maps correctly`() {
    val entity = UserEntity(
      id = "uid1",
      email = "test@red.local",
      passwordHash = "hash",
      fullName = "Test User",
      status = UserStatus.APPROVED,
      role = UserRole.ADMIN,
      phoneNumber = "+1234567890"
    )
    val view = UserView.from(entity)
    assertEquals("uid1", view.id)
    assertEquals("test@red.local", view.email)
    assertEquals("Test User", view.fullName)
    assertEquals(UserStatus.APPROVED, view.status)
    assertEquals(UserRole.ADMIN, view.role)
    assertEquals("+1234567890", view.phoneNumber)
    // Password hash should NOT be in the view
    assertFalse(view.toString().contains("hash"))
  }

  @Test
  fun `UserStatus enum values are correct`() {
    assertEquals(4, UserStatus.values().size)
    assertTrue(UserStatus.values().containsAll(listOf(
      UserStatus.PENDING, UserStatus.APPROVED, UserStatus.REJECTED, UserStatus.BANNED
    )))
  }
}
