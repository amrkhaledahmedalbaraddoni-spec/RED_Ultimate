package com.red.delivery

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for the BlockService logic.
 */
class BlockServiceTest {

  @Test
  fun `BlockDocument has correct fields`() {
    val doc = BlockDocument(
      id = "block-1",
      blockerId = "user1",
      blockeeId = "user2"
    )
    assertEquals("block-1", doc.id)
    assertEquals("user1", doc.blockerId)
    assertEquals("user2", doc.blockeeId)
    assertTrue(doc.createdAt > 0)
  }

  @Test
  fun `ConversationDto has correct fields`() {
    val dto = ConversationDto(
      conversationId = "conv1",
      peerId = "user2",
      lastTimestamp = 1000L,
      messageCount = 5L
    )
    assertEquals("conv1", dto.conversationId)
    assertEquals("user2", dto.peerId)
    assertEquals(5L, dto.messageCount)
  }
}

data class ConversationDto(
  val conversationId: String,
  val peerId: String,
  val lastTimestamp: Long,
  val messageCount: Long
)
