package com.red.delivery

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for ChatMeta DTOs and logic.
 */
class ChatMetaServiceTest {

  @Test
  fun `ChatMetaDocument has correct defaults`() {
    val doc = ChatMetaDocument(
      id = "user1_conv1",
      userId = "user1",
      conversationId = "conv1"
    )
    assertFalse(doc.isArchived)
    assertFalse(doc.isMuted)
    assertFalse(doc.isPinned)
    assertNull(doc.mutedUntil)
    assertNull(doc.pinnedAt)
  }

  @Test
  fun `ChatMetaDto from document maps correctly`() {
    val doc = ChatMetaDocument(
      id = "user1_conv1",
      userId = "user1",
      conversationId = "conv1",
      isArchived = true,
      isMuted = true,
      isPinned = false,
      mutedUntil = 123456789L
    )
    val dto = ChatMetaDto.from(doc)
    assertEquals("conv1", dto.conversationId)
    assertTrue(dto.isArchived)
    assertTrue(dto.isMuted)
    assertFalse(dto.isPinned)
    assertEquals(123456789L, dto.mutedUntil)
  }

  @Test
  fun `UpdateChatMetaRequest all fields nullable`() {
    val request = UpdateChatMetaRequest(isArchived = true, isMuted = null, isPinned = null)
    assertTrue(request.isArchived!!)
    assertNull(request.isMuted)
    assertNull(request.isPinned)
  }
}
