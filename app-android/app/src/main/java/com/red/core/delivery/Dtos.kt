package com.red.core.delivery

import com.squareup.moshi.JsonClass

/** Outgoing chat frame — mirrors the backend `IncomingMessage`. */
@JsonClass(generateAdapter = true)
data class ChatFrame(
  val messageId: String,
  val senderId: String,
  val receiverId: String,
  val conversationId: String,
  val payload: String, // base64 ciphertext
  val type: String = "TEXT",
  val timestamp: Long = System.currentTimeMillis()
)

enum class AckStatus { SENT, DUPLICATE, STORED, DELIVERED, FAILED }

@JsonClass(generateAdapter = true)
data class MessageAck(
  val messageId: String,
  val status: AckStatus,
  val sequenceNumber: Long? = null
)

enum class MessageStatus { SENDING, SENT, DELIVERED, READ, FAILED }
