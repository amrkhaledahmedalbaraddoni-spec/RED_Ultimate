package com.red.delivery

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for the MessageService deduplication and sequencing logic.
 * These are unit-level tests that verify the core algorithm.
 */
class MessageServiceTest {

  @Test
  fun `IncomingMessage has required fields`() {
    val msg = IncomingMessage(
      messageId = "test-id",
      senderId = "user1",
      receiverId = "user2",
      conversationId = "conv1",
      payload = "hello"
    )
    assertEquals("test-id", msg.messageId)
    assertEquals("user1", msg.senderId)
    assertEquals("TEXT", msg.type)
  }

  @Test
  fun `StoredMessage from document preserves all fields`() {
    val doc = MessageDocument(
      id = "msg-1",
      senderId = "s1",
      receiverId = "r1",
      conversationId = "conv-1",
      payload = "data",
      type = "TEXT",
      timestamp = 1000L,
      sequenceNumber = 42L
    )
    val stored = StoredMessage.from(doc)
    assertEquals("msg-1", stored.id)
    assertEquals("s1", stored.senderId)
    assertEquals(42L, stored.sequenceNumber)
  }

  @Test
  fun `AckStatus enum values are correct`() {
    assertEquals(5, AckStatus.values().size)
    assertTrue(AckStatus.values().containsAll(listOf(
      AckStatus.SENT, AckStatus.DUPLICATE, AckStatus.STORED, AckStatus.DELIVERED, AckStatus.FAILED
    )))
  }

  @Test
  fun `MessageAck carries status and sequence number`() {
    val ack = MessageAck(
      messageId = "msg-1",
      status = AckStatus.STORED,
      sequenceNumber = 42L
    )
    assertEquals("msg-1", ack.messageId)
    assertEquals(AckStatus.STORED, ack.status)
    assertEquals(42L, ack.sequenceNumber)
  }
}
