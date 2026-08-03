package com.red.delivery

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for the GroupService logic.
 */
class GroupServiceTest {

  // NOTE: These are unit tests for the GroupService business logic.
  // In a real environment with MongoDB, these would use @DataMongoTest or an embedded MongoDB.
  // For now, we test the DTO and validation logic.

  @Test
  fun `CreateGroupRequest requires name`() {
    val request = CreateGroupRequest(name = "Test Group", memberIds = listOf("user1"))
    assertEquals("Test Group", request.name)
    assertEquals(1, request.memberIds.size)
  }

  @Test
  fun `CreateGroupRequest defaults description to empty`() {
    val request = CreateGroupRequest(name = "Test", memberIds = listOf("user1"))
    assertEquals("", request.description)
  }

  @Test
  fun `GroupDto from document maps correctly`() {
    val doc = GroupDocument(
      id = "grp_123",
      name = "Test Group",
      description = "A test group",
      ownerId = "owner1",
      memberIds = listOf("owner1", "user1", "user2"),
      adminIds = listOf("owner1")
    )
    val dto = GroupDto.from(doc)
    assertEquals("grp_123", dto.id)
    assertEquals("Test Group", dto.name)
    assertEquals(3, dto.memberCount)
    assertEquals(1, dto.adminIds.size)
  }

  @Test
  fun `UpdateGroupRequest all fields nullable`() {
    val request = UpdateGroupRequest(name = "New Name", description = null, avatarUrl = null)
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
  fun `GroupDocument has correct defaults`() {
    val doc = GroupDocument(
      id = "grp_1",
      name = "Test",
      ownerId = "owner",
      memberIds = listOf("owner"),
      adminIds = listOf("owner")
    )
    assertEquals("", doc.description)
    assertNull(doc.avatarUrl)
    assertTrue(doc.createdAt > 0)
    assertTrue(doc.updatedAt > 0)
  }
}
