package com.red.delivery

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

/**
 * Message Reactions — emoji reactions on messages (like WhatsApp/Telegram).
 */

@Document(collection = "reactions")
data class ReactionDocument(
  @Id val id: String,  // messageId_userId
  val messageId: String,
  val userId: String,
  val emoji: String,
  val createdAt: Long = System.currentTimeMillis()
)

data class AddReactionRequest(
  val messageId: String,
  val emoji: String
)

data class ReactionDto(
  val messageId: String,
  val userId: String,
  val emoji: String,
  val createdAt: Long
) {
  companion object {
    fun from(doc: ReactionDocument) = ReactionDto(
      messageId = doc.messageId,
      userId = doc.userId,
      emoji = doc.emoji,
      createdAt = doc.createdAt
    )
  }
}

data class MessageReactionsDto(
  val messageId: String,
  val reactions: List<ReactionSummary>
)

data class ReactionSummary(
  val emoji: String,
  val count: Int,
  val hasMyReaction: Boolean
)

@Service
class ReactionService(
  private val mongoTemplate: MongoTemplate
) {

  fun addReaction(userId: String, request: AddReactionRequest): ReactionDocument {
    val id = "${request.messageId}_$userId"
    val doc = ReactionDocument(
      id = id,
      messageId = request.messageId,
      userId = userId,
      emoji = request.emoji
    )
    mongoTemplate.save(doc)
    return doc
  }

  fun removeReaction(userId: String, messageId: String) {
    val id = "${messageId}_$userId"
    val query = Query(Criteria.where("id").isEqualTo(id))
    mongoTemplate.remove(query, ReactionDocument::class.java)
  }

  fun getReactionsForMessage(messageId: String): List<ReactionDocument> {
    val query = Query(Criteria.where("messageId").isEqualTo(messageId))
    return mongoTemplate.find(query, ReactionDocument::class.java)
  }

  fun getMessageReactionsSummary(messageId: String, currentUserId: String): MessageReactionsDto {
    val reactions = getReactionsForMessage(messageId)
    val summaries = reactions
      .groupBy { it.emoji }
      .map { (emoji, docs) ->
        ReactionSummary(
          emoji = emoji,
          count = docs.size,
          hasMyReaction = docs.any { it.userId == currentUserId }
        )
      }
      .sortedByDescending { it.count }
    return MessageReactionsDto(messageId = messageId, reactions = summaries)
  }

  fun getReactionsForMultipleMessages(messageIds: List<String>, currentUserId: String): Map<String, MessageReactionsDto> {
    return messageIds.associateWith { messageId ->
      getMessageReactionsSummary(messageId, currentUserId)
    }
  }
}

@RestController
@RequestMapping("/api/reactions")
class ReactionController(
  private val reactionService: ReactionService
) {

  @PostMapping
  fun addReaction(authentication: Authentication, @RequestBody req: AddReactionRequest): ReactionDto {
    val doc = reactionService.addReaction(authentication.name, req)
    return ReactionDto.from(doc)
  }

  @DeleteMapping("/{messageId}")
  fun removeReaction(authentication: Authentication, @PathVariable messageId: String) {
    reactionService.removeReaction(authentication.name, messageId)
  }

  @GetMapping("/{messageId}")
  fun getReactions(authentication: Authentication, @PathVariable messageId: String): MessageReactionsDto {
    return reactionService.getMessageReactionsSummary(messageId, authentication.name)
  }

  @PostMapping("/batch")
  fun getBatchReactions(
    authentication: Authentication,
    @RequestBody messageIds: List<String>
  ): Map<String, MessageReactionsDto> {
    return reactionService.getReactionsForMultipleMessages(messageIds, authentication.name)
  }
}
