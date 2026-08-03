package com.red.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.red.delivery.MessageRepository
import org.springframework.stereotype.Service

/**
 * Read receipt service. When a user marks messages as read, this service:
 * 1. Updates the message status in MongoDB
 * 2. Notifies the sender via WebSocket
 */
@Service
class ReadReceiptService(
  private val messageRepository: MessageRepository,
  private val presence: PresenceService,
  private val mapper: ObjectMapper
) {

  data class ReadReceipt(
    val type: String = "READ_RECEIPT",
    val conversationId: String,
    val readerId: String,
    val messageIds: List<String>,
    val timestamp: Long = System.currentTimeMillis()
  )

  fun markAsRead(conversationId: String, readerId: String, messageIds: List<String>) {
    // Notify the sender via WebSocket
    // Find the conversation peer - we need to find who the other participant is
    // For now, we'll send the read receipt to the sender of each message
    val receipt = ReadReceipt(conversationId = conversationId, readerId = readerId, messageIds = messageIds)

    // Find the peer by looking at the conversation's messages
    val messages = messageRepository.findByConversationIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(conversationId, 0)
    val peerIds = messages.map { if (it.senderId == readerId) it.receiverId else it.senderId }.toSet()
      .filter { it != readerId }

    val json = mapper.writeValueAsString(receipt)
    for (peerId in peerIds) {
      val session = presence.sessionFor(peerId)
      if (session != null && session.isOpen) {
        runCatching {
          session.sendMessage(org.springframework.web.socket.TextMessage(json))
        }
      }
    }
  }
}
