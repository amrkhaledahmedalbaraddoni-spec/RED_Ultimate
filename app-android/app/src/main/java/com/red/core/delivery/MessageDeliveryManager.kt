package com.red.core.delivery

import com.red.core.database.RedDatabase
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
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
  private val moshi: Moshi
) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val sendLock = Mutex()
  private val connected = MutableStateFlow(false)
  private val frameAdapter = moshi.adapter(ChatFrame::class.java)
  private val ackAdapter = moshi.adapter(MessageAck::class.java)

  fun start() {
    client.setListener { raw -> scope.launch { handleFrame(raw) } }
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

  private suspend fun handleFrame(raw: String) {
    // Try as ACK first
    val ack = runCatching { ackAdapter.fromJson(raw) }.getOrNull()
    if (ack != null && ack.status != null) {
      onAckReceived(ack)
      return
    }
    // Otherwise treat as incoming message
    val frame = runCatching { frameAdapter.fromJson(raw) }.getOrNull() ?: return
    val entity = MessageEntity(
      id = frame.messageId,
      conversationId = frame.conversationId,
      senderId = frame.senderId,
      receiverId = identity.userId,
      payload = frame.payload,
      timestamp = frame.timestamp,
      status = MessageStatus.DELIVERED
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
