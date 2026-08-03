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
        .copy(senderId = senderId)
    }.getOrElse {
      ack(session, MessageAck(message.payload.take(64), AckStatus.FAILED))
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
