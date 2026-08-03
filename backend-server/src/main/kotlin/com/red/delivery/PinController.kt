package com.red.delivery

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

/**
 * Message Pinning — pin important messages in conversations.
 */

data class PinMessageRequest(
  val messageId: String,
  val conversationId: String
)

data class PinnedMessageDto(
  val messageId: String,
  val conversationId: String,
  val pinnedBy: String,
  val pinnedAt: Long
)

@Service
class PinService(
  private val mongoTemplate: MongoTemplate
) {

  fun pinMessage(userId: String, request: PinMessageRequest): PinnedMessageDto {
    val query = Query(Criteria.where("messageId").isEqualTo(request.messageId))
    val update = Update()
      .set("conversationId", request.conversationId)
      .set("pinnedBy", userId)
      .set("pinnedAt", System.currentTimeMillis())
    mongoTemplate.upsert(query, update, "pinned_messages")
    return PinnedMessageDto(
      messageId = request.messageId,
      conversationId = request.conversationId,
      pinnedBy = userId,
      pinnedAt = System.currentTimeMillis()
    )
  }

  fun unpinMessage(messageId: String) {
    val query = Query(Criteria.where("messageId").isEqualTo(messageId))
    mongoTemplate.remove(query, "pinned_messages")
  }

  fun getPinnedMessages(conversationId: String): List<PinnedMessageDto> {
    val query = Query(Criteria.where("conversationId").isEqualTo(conversationId))
    val docs = mongoTemplate.find(query, org.bson.Document::class.java, "pinned_messages")
    return docs.map { doc ->
      PinnedMessageDto(
        messageId = doc.getString("messageId"),
        conversationId = doc.getString("conversationId"),
        pinnedBy = doc.getString("pinnedBy"),
        pinnedAt = doc.getLong("pinnedAt") ?: 0L
      )
    }
  }
}

@RestController
@RequestMapping("/api/pins")
class PinController(
  private val pinService: PinService
) {

  @PostMapping
  fun pinMessage(authentication: Authentication, @RequestBody req: PinMessageRequest): PinnedMessageDto {
    return pinService.pinMessage(authentication.name, req)
  }

  @DeleteMapping("/{messageId}")
  fun unpinMessage(@PathVariable messageId: String) {
    pinService.unpinMessage(messageId)
  }

  @GetMapping("/{conversationId}")
  fun getPinnedMessages(@PathVariable conversationId: String): List<PinnedMessageDto> {
    return pinService.getPinnedMessages(conversationId)
  }
}
