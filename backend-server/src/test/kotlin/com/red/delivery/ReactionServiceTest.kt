package com.red.delivery

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for Reaction DTOs.
 */
class ReactionServiceTest {

  @Test
  fun `AddReactionRequest has required fields`() {
    val request = AddReactionRequest(messageId = "msg_123", emoji = "👍")
    assertEquals("msg_123", request.messageId)
    assertEquals("👍", request.emoji)
  }

  @Test
  fun `ReactionDto from document maps correctly`() {
    val doc = ReactionDocument(
      id = "msg_123_user1",
      messageId = "msg_123",
      userId = "user1",
      emoji = "❤️"
    )
    val dto = ReactionDto.from(doc)
    assertEquals("msg_123", dto.messageId)
    assertEquals("user1", dto.userId)
    assertEquals("❤️", dto.emoji)
  }

  @Test
  fun `ReactionSummary counts correctly`() {
    val summary = ReactionSummary(emoji = "👍", count = 3, hasMyReaction = true)
    assertEquals("👍", summary.emoji)
    assertEquals(3, summary.count)
    assertTrue(summary.hasMyReaction)
  }

  @Test
  fun `MessageReactionsDto groups reactions`() {
    val dto = MessageReactionsDto(
      messageId = "msg_1",
      reactions = listOf(
        ReactionSummary("👍", 2, true),
        ReactionSummary("❤️", 1, false)
      )
    )
    assertEquals("msg_1", dto.messageId)
    assertEquals(2, dto.reactions.size)
  }

  @Test
  fun `DisappearingConfigDto defaults`() {
    val dto = DisappearingConfigDto(
      conversationId = "conv_1",
      durationSeconds = 0L,
      enabled = false
    )
    assertFalse(dto.enabled)
    assertEquals(0L, dto.durationSeconds)
  }

  @Test
  fun `SetDisappearingRequest has required fields`() {
    val request = SetDisappearingRequest(
      conversationId = "conv_1",
      durationSeconds = 86400L
    )
    assertEquals("conv_1", request.conversationId)
    assertEquals(86400L, request.durationSeconds)
  }
}
