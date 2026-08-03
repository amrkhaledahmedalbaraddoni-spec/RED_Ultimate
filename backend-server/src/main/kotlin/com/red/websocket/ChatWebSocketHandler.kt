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

/**
 * Real-time chat channel.
 *
 * Order of operations on each inbound message:
 *   1. parse JSON
 *   2. persist + de-dup + sequence via [MessageService]
 *   3. ACK the sender (with the assigned sequence number, or DUPLICATE)
 *   4. relay to the recipient if currently connected; otherwise the message is safely stored for
 *      offline sync.
 */
@Component
class ChatWebSocketHandler(
  private val messageService: MessageService,
  private val presence: PresenceService,
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
    val incoming = runCatching {
      mapper.readValue(message.payload, IncomingMessage::class.java)
        .copy(senderId = senderId) // never trust the client's claimed sender
    }.getOrElse {
      ack(session, MessageAck(message.payload.take(64), AckStatus.FAILED))
      return
    }

    val seq = messageService.processIncoming(incoming)
    if (seq == -1L) {
      ack(session, MessageAck(incoming.messageId ?: "", AckStatus.DUPLICATE))
      return
    }

    ack(session, MessageAck(incoming.messageId ?: "", AckStatus.STORED, seq))

    val recipientSession = presence.sessionFor(incoming.receiverId)
    if (recipientSession != null && recipientSession.isOpen) {
      val relay = mapper.writeValueAsString(
        incoming.copy(messageId = incoming.messageId ?: incoming.conversationId)
      )
      runCatching { recipientSession.sendMessage(TextMessage(relay)) }
    }
    // else: message is persisted; the client will fetch via the sync endpoint on reconnect.
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
}
