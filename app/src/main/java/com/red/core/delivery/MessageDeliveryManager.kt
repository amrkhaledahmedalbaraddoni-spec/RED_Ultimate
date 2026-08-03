package com.red.core.delivery

import com.red.core.database.RedDatabase
import com.red.core.security.SessionManager
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDeliveryManager @Inject constructor(
  private val database: RedDatabase,
  private val client: DevelopedWebSocketClient,
  private val identity: ClientIdentity,
  private val moshi: Moshi,
  private val sessionManager: SessionManager
) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val sendLock = Mutex()
  private val connected = MutableStateFlow(false)
  val connectionState: StateFlow<Boolean> = connected
  private val frameAdapter = moshi.adapter(ChatFrame::class.java)
  private val ackAdapter = moshi.adapter(MessageAck::class.java)
  private val typingAdapter = moshi.adapter(TypingFrame::class.java)
  private val readAdapter = moshi.adapter(ReadFrame::class.java)

  fun start() {
    client.setListener { raw -> scope.launch { handleFrame(raw) } }
    client.setConnectionListener { isConnected -> connected.value = isConnected }
    client.connect()
  }

  fun stop() {
    client.close()
    connected.value = false
  }

  fun sendMessage(
    conversationId: String,
    receiverId: String,
    encryptedPayload: String,
    type: String = "TEXT"
  ) {
    val frame = ChatFrame(
      messageId = UuidV7.now(),
      senderId = identity.userId,
      receiverId = receiverId,
      conversationId = conversationId,
      payload = encryptedPayload,
      type = type
    )
    scope.launch {
      val entity = MessageEntity(
        id = frame.messageId,
        conversationId = conversationId,
        senderId = identity.userId,
        receiverId = receiverId,
        payload = sessionManager.encryptMessage(encryptedPayload),
        timestamp = frame.timestamp,
        status = MessageStatus.SENDING,
        type = type
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

  /** Send a typing indicator. */
  fun sendTyping(conversationId: String, isTyping: Boolean) {
    scope.launch {
      if (client is DevelopedWebSocketClientImpl) {
        client.sendTyping(conversationId, isTyping)
      }
    }
  }

  /** Send a read receipt. */
  fun sendReadReceipt(conversationId: String, messageIds: List<String>) {
    scope.launch {
      if (client is DevelopedWebSocketClientImpl) {
        client.sendReadReceipt(conversationId, messageIds)
      }
    }
  }

  private suspend fun handleFrame(raw: String) {
    // Try as ACK first
    val ack = runCatching { ackAdapter.fromJson(raw) }.getOrNull()
    if (ack != null && ack.status != null) {
      onAckReceived(ack)
      return
    }
    // Try as typing indicator
    val typing = runCatching { typingAdapter.fromJson(raw) }.getOrNull()
    if (typing != null && typing.type == "TYPING") {
      // Typing event received from peer — UI observes via ViewModel
      return
    }
    // Try as read receipt
    val read = runCatching { readAdapter.fromJson(raw) }.getOrNull()
    if (read != null && read.type == "READ") {
      // Update message statuses
      for (msgId in read.messageIds) {
        database.messageDao().updateStatus(msgId, MessageStatus.READ)
      }
      return
    }
    // Otherwise treat as incoming message
    val frame = runCatching { frameAdapter.fromJson(raw) }.getOrNull() ?: return
    val entity = MessageEntity(
      id = frame.messageId,
      conversationId = frame.conversationId,
      senderId = frame.senderId,
      receiverId = identity.userId,
      payload = sessionManager.encryptMessage(frame.payload),
      timestamp = frame.timestamp,
      status = MessageStatus.DELIVERED,
      type = frame.type
    )
    database.messageDao().upsert(entity)
  }

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

@Singleton
class ClientIdentity @Inject constructor() {
  var userId: String = ""
  var token: String = ""
}
