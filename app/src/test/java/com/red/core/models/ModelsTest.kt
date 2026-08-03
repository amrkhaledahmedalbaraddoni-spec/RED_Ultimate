package com.red.core.models

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the data models.
 */
class ModelsTest {

    @Test
    fun `UserStatus enum should have all values`() {
        assertEquals(4, UserStatus.values().size)
        assertNotNull(UserStatus.valueOf("PENDING"))
        assertNotNull(UserStatus.valueOf("APPROVED"))
        assertNotNull(UserStatus.valueOf("REJECTED"))
        assertNotNull(UserStatus.valueOf("BANNED"))
    }

    @Test
    fun `User data class should have default values`() {
        val user = User(
            id = "1",
            email = "test@test.com",
            fullName = "Test User",
            status = UserStatus.APPROVED
        )
        assertEquals("USER", user.role)
        assertEquals(0L, user.createdAt)
        assertNull(user.phoneNumber)
        assertNull(user.avatarUrl)
    }

    @Test
    fun `AuthResponse should contain token and user`() {
        val user = User("1", "test@test.com", "Test", UserStatus.APPROVED)
        val response = AuthResponse("jwt_token", user)
        assertEquals("jwt_token", response.token)
        assertEquals("1", response.user.id)
    }

    @Test
    fun `PublicUserDto should not expose sensitive fields`() {
        val dto = PublicUserDto("1", "Test User", UserStatus.APPROVED)
        // Should not have email, phone, etc.
        assertFalse(dto::class.members.any { it.name == "email" })
        assertFalse(dto::class.members.any { it.name == "phoneNumber" })
    }
}
