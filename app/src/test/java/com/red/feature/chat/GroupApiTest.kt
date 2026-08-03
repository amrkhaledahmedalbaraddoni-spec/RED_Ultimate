package com.red.feature.chat

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Group API DTOs.
 */
class GroupApiTest {

  @Test
  fun `CreateGroupRequest has correct fields`() {
    val request = CreateGroupRequest(name = "Test Group", description = "A test", memberIds = listOf("user1", "user2"))
    assertEquals("Test Group", request.name)
    assertEquals("A test", request.description)
    assertEquals(2, request.memberIds.size)
  }

  @Test
  fun `CreateGroupRequest defaults description to empty`() {
    val request = CreateGroupRequest(name = "Test", memberIds = listOf("user1"))
    assertEquals("", request.description)
  }

  @Test
  fun `GroupDto has all fields`() {
    val dto = GroupDto(
      id = "grp_123",
      name = "Test Group",
      description = "A test",
      avatarUrl = "https://example.com/avatar.jpg",
      ownerId = "owner1",
      memberIds = listOf("owner1", "user1", "user2"),
      adminIds = listOf("owner1"),
      createdAt = 123456789L,
      memberCount = 3
    )
    assertEquals("grp_123", dto.id)
    assertEquals("Test Group", dto.name)
    assertEquals(3, dto.memberCount)
    assertEquals(1, dto.adminIds.size)
    assertNotNull(dto.avatarUrl)
  }

  @Test
  fun `UpdateGroupRequest all fields nullable`() {
    val request = UpdateGroupRequest(name = "New Name")
    assertEquals("New Name", request.name)
    assertNull(request.description)
    assertNull(request.avatarUrl)
  }

  @Test
  fun `AddMemberRequest has userId`() {
    val request = AddMemberRequest(userId = "user3")
    assertEquals("user3", request.userId)
  }

  @Test
  fun `PromoteAdminRequest has userId`() {
    val request = PromoteAdminRequest(userId = "user1")
    assertEquals("user1", request.userId)
  }

  @Test
  fun `ChatMetaDto has correct fields`() {
    val dto = ChatMetaDto(
      conversationId = "conv1",
      isArchived = true,
      isMuted = false,
      isPinned = true,
      mutedUntil = null
    )
    assertEquals("conv1", dto.conversationId)
    assertTrue(dto.isArchived)
    assertFalse(dto.isMuted)
    assertTrue(dto.isPinned)
    assertNull(dto.mutedUntil)
  }

  @Test
  fun `UpdateChatMetaRequest all fields nullable`() {
    val request = UpdateChatMetaRequest(isArchived = true)
    assertTrue(request.isArchived!!)
    assertNull(request.isMuted)
    assertNull(request.isPinned)
    assertNull(request.mutedUntil)
  }

  @Test
  fun `SearchResultsDto has correct structure`() {
    val dto = SearchResultsDto(
      messages = emptyList(),
      users = emptyList(),
      groups = emptyList(),
      total = 0
    )
    assertEquals(0, dto.total)
    assertTrue(dto.messages.isEmpty())
    assertTrue(dto.users.isEmpty())
    assertTrue(dto.groups.isEmpty())
  }

  @Test
  fun `UserSearchResultDto has required fields`() {
    val dto = UserSearchResultDto(id = "user1", fullName = "Test User", email = "test@example.com")
    assertEquals("user1", dto.id)
    assertEquals("Test User", dto.fullName)
    assertEquals("test@example.com", dto.email)
  }

  @Test
  fun `GroupSearchResultDto has required fields`() {
    val dto = GroupSearchResultDto(id = "grp_1", name = "Test Group", memberCount = 5)
    assertEquals("grp_1", dto.id)
    assertEquals("Test Group", dto.name)
    assertEquals(5, dto.memberCount)
  }
}
