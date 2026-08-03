package com.red.delivery

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * Persisted chat message. The [payload] is the opaque, end-to-end-encrypted ciphertext blob; the
 * server never reads message content.
 */
@Document(collection = "messages")
class MessageDocument(
  @Id
  val id: String, // UUIDv7 — also used as the dedup key

  @Indexed
  val senderId: String,

  @Indexed
  val receiverId: String,

  @Indexed
  val conversationId: String,

  /** Base64-encoded ciphertext. */
  val payload: String,

  val type: String,

  val timestamp: Long,

  @Indexed
  val sequenceNumber: Long
)

interface MessageRepository : MongoRepository<MessageDocument, String> {
  fun findByConversationIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
    conversationId: String,
    sequenceNumber: Long
  ): List<MessageDocument>

  fun findByReceiverIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
    receiverId: String,
    sequenceNumber: Long
  ): List<MessageDocument>

  fun countByTimestampGreaterThan(threshold: Long): Long
}

@Document(collection = "stories")
class StoryDocument(
  @Id
  val id: String,
  @Indexed
  val ownerId: String,
  val mediaUrl: String,
  val createdAt: Long,
  val expiresAt: Long
)

interface StoryRepository : MongoRepository<StoryDocument, String> {
  fun deleteByExpiresAtLessThan(threshold: Long): Long
  fun countByExpiresAtGreaterThan(threshold: Long): Long
}
