package com.red.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.red.delivery.AckStatus
import com.red.delivery.IncomingMessage
import com.red.delivery.MessageAck
import com.red.delivery.MessageService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ChatWebSocketHandler(
  private val messageService: MessageService,
  private val presence: PresenceService,
  private val readReceiptService: ReadReceiptService,
  private val mapper: ObjectMapper
) : TextWebSocketHandler() {

  override fun afterConnectionEstablished(session: WebSocketSession) {
    val userId = session.attributes["userId"] as? String ?: run {
      runCatching { session.close(CloseStatus.POLICY_VIOLATION) }
      return
    }
    presence.register(userId, session)
  }

  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    val senderId = session.attributes["userId"] as? String ?: return
    val payload = message.payload

    // Try to parse as a typed JSON object with a "type" discriminator
    val tree = runCatching { mapper.readTree(payload) }.getOrNull() ?: return
    val type = tree.get("type")?.asText() ?: ""

    when (type) {
      "TYPING" -> {
        val conversationId = tree.get("conversationId")?.asText() ?: return
        // Forward to the peer
        val peerIds = findConversationPeers(conversationId, senderId)
        for (peerId in peerIds) {
          val peerSession = presence.sessionFor(peerId)
          if (peerSession != null && peerSession.isOpen) {
            runCatching { peerSession.sendMessage(TextMessage(payload)) }
          }
        }
        return
      }

      "READ" -> {
        val conversationId = tree.get("conversationId")?.asText() ?: return
        val messageIds = tree.get("messageIds")?.map { it.asText() } ?: return
        readReceiptService.markAsRead(conversationId, senderId, messageIds)
        return
      }

      else -> {
        // Regular chat message
        val incoming = runCatching {
          mapper.readValue(payload, IncomingMessage::class.java)
            .copy(senderId = senderId)
        }.getOrElse {
          ack(session, MessageAck(payload.take(64), AckStatus.FAILED))
          return
        }

        val result = messageService.processIncoming(incoming)
        if (result.duplicate) {
          ack(session, MessageAck(result.id, AckStatus.DUPLICATE))
          return
        }

        ack(session, MessageAck(result.id, AckStatus.STORED, result.sequenceNumber))

        val recipientSession = presence.sessionFor(incoming.receiverId)
        if (recipientSession != null && recipientSession.isOpen) {
          val relay = mapper.writeValueAsString(
            incoming.copy(messageId = result.id)
          )
          runCatching { recipientSession.sendMessage(TextMessage(relay)) }
        }
      }
    }
  }

  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    val userId = session.attributes["userId"] as? String ?: return
    presence.unregister(userId, session)
  }

  private fun ack(session: WebSocketSession, ack: MessageAck) {
    if (session.isOpen) {
      runCatching { session.sendMessage(TextMessage(mapper.writeValueAsString(ack))) }
    }
  }

  private fun findConversationPeers(conversationId: String, excludeUserId: String): List<String> {
    // Simple heuristic: conversation IDs are typically "userId1:userId2"
    val parts = conversationId.split(":")
    return parts.filter { it != excludeUserId }
  }
}
