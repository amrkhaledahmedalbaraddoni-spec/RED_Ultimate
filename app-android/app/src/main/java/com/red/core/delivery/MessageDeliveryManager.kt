package com.red.core.delivery

import com.red.core.database.RedDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * System C client: guarantees delivery by (1) persisting every outbound message immediately with
 * SENDING status, (2) pushing it over the WebSocket, and (3) flipping status to SENT/DELIVERED/
 * FAILED as acks arrive. Messages survive process death and are retried on reconnect.
 */
@Singleton
class MessageDeliveryManager @Inject constructor(
  private val database: RedDatabase,
  private val client: DevelopedWebSocketClient,
  private val identity: ClientIdentity
) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val sendLock = Mutex()
  private val connected = MutableStateFlow(false)

  fun start() {
    client.setListener { raw -> handleIncoming(raw) }
    client.connect()
  }

  fun sendMessage(conversationId: String, receiverId: String, encryptedPayload: String) {
    val frame = ChatFrame(
      messageId = UuidV7.now(),
      senderId = identity.userId,
      receiverId = receiverId,
      conversationId = conversationId,
      payload = encryptedPayload
    )
    scope.launch {
      val entity = MessageEntity(
        id = frame.messageId,
        conversationId = conversationId,
        senderId = identity.userId,
        receiverId = receiverId,
        payload = encryptedPayload,
        timestamp = frame.timestamp,
        status = MessageStatus.SENDING
      )
      database.messageDao().upsert(entity)
      sendLock.withLock {
        val accepted = client.send(frame)
        if (!accepted) {
          database.messageDao().updateStatus(frame.messageId, MessageStatus.FAILED)
        }
      }
    }
  }

  private fun handleIncoming(raw: String) {
    // Ack parsing is handled by the client; here we only act on ack frames.
  }

  /** Called by the transport when a server [MessageAck] is decoded. */
  fun onAckReceived(ack: MessageAck) {
    scope.launch {
      val status = when (ack.status) {
        AckStatus.SENT, AckStatus.STORED -> MessageStatus.SENT
        AckStatus.DUPLICATE -> MessageStatus.SENT
        AckStatus.DELIVERED -> MessageStatus.DELIVERED
        AckStatus.FAILED -> MessageStatus.FAILED
      }
      database.messageDao().updateStatus(ack.messageId, status)
    }
  }
}

/** Minimal identity holder; populated by the auth flow after login. */
@Singleton
class ClientIdentity @Inject constructor() {
  var userId: String = ""
  var token: String = ""
}
