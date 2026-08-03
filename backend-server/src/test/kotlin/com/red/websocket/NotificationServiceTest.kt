package com.red.websocket

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for the NotificationService and ReadReceiptService.
 */
class NotificationServiceTest {

  @Test
  fun `PendingNotification has correct fields`() {
    val notification = NotificationService.PendingNotification(
      userId = "user1",
      type = "MESSAGE",
      content = "You have a new message"
    )
    assertEquals("user1", notification.userId)
    assertEquals("MESSAGE", notification.type)
    assertTrue(notification.timestamp > 0)
  }

  @Test
  fun `ReadReceipt has correct fields`() {
    val receipt = ReadReceiptService.ReadReceipt(
      conversationId = "conv1",
      readerId = "user1",
      messageIds = listOf("msg1", "msg2", "msg3")
    )
    assertEquals("conv1", receipt.conversationId)
    assertEquals("user1", receipt.readerId)
    assertEquals(3, receipt.messageIds.size)
    assertTrue(receipt.timestamp > 0)
  }
}
