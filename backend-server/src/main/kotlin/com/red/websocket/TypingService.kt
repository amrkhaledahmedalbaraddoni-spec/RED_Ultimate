package com.red.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

/**
 * Real-time typing indicator service.
 * Broadcasts typing events to the conversation peer via WebSocket.
 * Also publishes to Redis for cross-instance fan-out.
 */
@Service
class TypingService(
  private val presence: PresenceService,
  private val redis: StringRedisTemplate,
  private val mapper: ObjectMapper
) {

  fun broadcastTyping(senderId: String, conversationId: String) {
    val payload = mapOf(
      "type" to "TYPING",
      "senderId" to senderId,
      "conversationId" to conversationId,
      "timestamp" to System.currentTimeMillis()
    )
    val json = mapper.writeValueAsString(payload)

    // Publish to Redis for cross-instance fan-out
    redis.convertAndSend("chat:typing:$conversationId", json)

    // Also deliver locally to any online peer
    val peerIds = findConversationPeers(conversationId, senderId)
    for (peerId in peerIds) {
      val session = presence.sessionFor(peerId)
      if (session != null && session.isOpen) {
        runCatching {
          session.sendMessage(org.springframework.web.socket.TextMessage(json))
        }
      }
    }
  }

  private fun findConversationPeers(conversationId: String, excludeUserId: String): List<String> {
    val parts = conversationId.split(":")
    return parts.filter { it != excludeUserId }
  }
}
