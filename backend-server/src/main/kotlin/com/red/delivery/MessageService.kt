package com.red.delivery

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * System C: Guaranteed Delivery.
 *
 * Pipeline: Receive -> De-duplicate (Redis, 24h TTL) -> Sequence (Redis INCR per conversation) ->
 * Persist (MongoDB) -> Return [Result] with the canonical id and sequence number.
 */
@Service
class MessageService(
  private val messageRepository: MessageRepository,
  private val redis: StringRedisTemplate
) {

  data class Result(val duplicate: Boolean, val id: String, val sequenceNumber: Long)

  fun processIncoming(message: IncomingMessage, forceId: String = UuidV7.now()): Result {
    val id = message.messageId?.takeIf { it.isNotBlank() } ?: forceId

    val dedupKey = "msg:dedup:$id"
    val stored = redis.opsForValue().setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS) ?: false
    if (!stored) {
      return Result(duplicate = true, id = id, sequenceNumber = -1L)
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
    return Result(duplicate = false, id = id, sequenceNumber = seq)
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
