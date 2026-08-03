package com.red.delivery

/** JSON contract shared with the Android client over the WebSocket. */
data class IncomingMessage(
  val messageId: String? = null,
  val senderId: String,
  val receiverId: String,
  val conversationId: String,
  val payload: String, // base64 ciphertext
  val type: String = "TEXT",
  val timestamp: Long = System.currentTimeMillis()
)

data class MessageAck(
  val messageId: String,
  val status: AckStatus,
  val sequenceNumber: Long? = null
)

enum class AckStatus { SENT, DUPLICATE, STORED, DELIVERED, FAILED }

/** Compact projection returned to clients fetching missed messages. */
data class StoredMessage(
  val id: String,
  val senderId: String,
  val receiverId: String,
  val conversationId: String,
  val payload: String,
  val type: String,
  val timestamp: Long,
  val sequenceNumber: Long
) {
  companion object {
    fun from(doc: MessageDocument) = StoredMessage(
      id = doc.id,
      senderId = doc.senderId,
      receiverId = doc.receiverId,
      conversationId = doc.conversationId,
      payload = doc.payload,
      type = doc.type,
      timestamp = doc.timestamp,
      sequenceNumber = doc.sequenceNumber
    )
  }
}
