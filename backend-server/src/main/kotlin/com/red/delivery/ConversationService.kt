package com.red.delivery

import org.springframework.stereotype.Service

@Service
class ConversationService(
  private val messageRepository: MessageRepository
) {

  data class ConversationSummary(
    val conversationId: String,
    val peerId: String,
    val lastTimestamp: Long,
    val messageCount: Long
  )

  fun listForUser(userId: String): List<ConversationSummary> {
    val messages = messageRepository.findBySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId)
    return messages
      .groupBy { it.conversationId }
      .map { (convId, msgs) ->
        val last = msgs.first()
        val peerId = if (last.senderId == userId) last.receiverId else last.senderId
        ConversationSummary(
          conversationId = convId,
          peerId = peerId,
          lastTimestamp = last.timestamp,
          messageCount = msgs.size.toLong()
        )
      }
      .sortedByDescending { it.lastTimestamp }
  }
}
