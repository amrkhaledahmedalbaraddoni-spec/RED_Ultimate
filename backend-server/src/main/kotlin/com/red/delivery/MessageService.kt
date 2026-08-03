package com.red.delivery

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * System C: Guaranteed Delivery.
 *
 * Pipeline: Receive -> De-duplicate (Redis, 24h TTL) -> Sequence (Redis INCR per conversation) ->
 * Persist (MongoDB) -> Return sequence number. Returns -1 for duplicates so the caller can ACK
 * accordingly.
 */
@Service
class MessageService(
  private val messageRepository: MessageRepository,
  private val redis: StringRedisTemplate
) {

  fun processIncoming(message: IncomingMessage, forceId: String = UuidV7.now()): Long {
    val id = message.messageId?.takeIf { it.isNotBlank() } ?: forceId

    val dedupKey = "msg:dedup:$id"
    // SETNX with TTL: atomic "first writer wins" de-duplication.
    val stored = redis.opsForValue().setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS) ?: false
    if (!stored) {
      return -1L
    }

    val seq = redis.opsForValue().increment("conv:seq:${message.conversationId}") ?: 0L

    val doc = MessageDocument(
      id = id,
      senderId = message.senderId,
      receiverId = message.receiverId,
      conversationId = message.conversationId,
      payload = message.payload,
      type = message.type,
      timestamp = message.timestamp,
      sequenceNumber = seq
    )
    messageRepository.save(doc)
    return seq
  }

  fun messagesForConversation(conversationId: String, since: Long): List<StoredMessage> =
    messageRepository
      .findByConversationIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(conversationId, since)
      .map { StoredMessage.from(it) }

  fun pendingForUser(userId: String, since: Long): List<StoredMessage> =
    messageRepository
      .findByReceiverIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(userId, since)
      .map { StoredMessage.from(it) }

  fun countSince(threshold: Long): Long = messageRepository.countByTimestampGreaterThan(threshold)
}
