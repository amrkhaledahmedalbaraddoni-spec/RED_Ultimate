package com.red.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.red.delivery.MessageAck
import com.red.delivery.AckStatus
import org.springframework.stereotype.Service

/**
 * Real-time typing indicator service.
 * Broadcasts typing events to the conversation peer via WebSocket.
 * Also publishes to Redis for cross-instance fan-out.
 */
@Service
class TypingService(
  private val presence: PresenceService,
  private val redis: org.springframework.data.redis.core.StringRedisTemplate,
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

    // Find the peer in this conversation and send if online
    // We need to look up who the peer is - for now we publish to Redis
    // The ChatWebSocketHandler will handle local delivery
    redis.convertAndSend("chat:typing:$conversationId", json)
  }

  private fun org.springframework.data.redis.core.StringRedisTemplate.convertAndSend(channel: String, message: String) {
    this.convertAndSend(channel, message)
  }
}
