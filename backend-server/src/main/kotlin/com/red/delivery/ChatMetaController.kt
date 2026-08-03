package com.red.delivery

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

/**
 * Chat metadata — archive, mute, pin conversations per user.
 */

@Document(collection = "chat_meta")
data class ChatMetaDocument(
  @Id val id: String,  // composite: userId_conversationId
  val userId: String,
  val conversationId: String,
  val isArchived: Boolean = false,
  val isMuted: Boolean = false,
  val isPinned: Boolean = false,
  val mutedUntil: Long? = null,  // timestamp for temporary mute
  val pinnedAt: Long? = null
)

data class UpdateChatMetaRequest(
  val isArchived: Boolean? = null,
  val isMuted: Boolean? = null,
  val isPinned: Boolean? = null,
  val mutedUntil: Long? = null
)

data class ChatMetaDto(
  val conversationId: String,
  val isArchived: Boolean,
  val isMuted: Boolean,
  val isPinned: Boolean,
  val mutedUntil: Long?
) {
  companion object {
    fun from(doc: ChatMetaDocument) = ChatMetaDto(
      conversationId = doc.conversationId,
      isArchived = doc.isArchived,
      isMuted = doc.isMuted,
      isPinned = doc.isPinned,
      mutedUntil = doc.mutedUntil
    )
  }
}

@Service
class ChatMetaService(
  private val mongoTemplate: MongoTemplate
) {

  fun getMeta(userId: String, conversationId: String): ChatMetaDocument? {
    val id = "${userId}_$conversationId"
    return mongoTemplate.findById(id, ChatMetaDocument::class.java)
  }

  fun listArchived(userId: String): List<ChatMetaDocument> {
    val query = Query(Criteria.where("userId").isEqualTo(userId).and("isArchived").isEqualTo(true))
    return mongoTemplate.find(query, ChatMetaDocument::class.java)
  }

  fun listMuted(userId: String): List<ChatMetaDocument> {
    val query = Query(Criteria.where("userId").isEqualTo(userId).and("isMuted").isEqualTo(true))
    return mongoTemplate.find(query, ChatMetaDocument::class.java)
  }

  fun listPinned(userId: String): List<ChatMetaDocument> {
    val query = Query(Criteria.where("userId").isEqualTo(userId).and("isPinned").isEqualTo(true))
    return mongoTemplate.find(query, ChatMetaDocument::class.java)
  }

  fun updateMeta(userId: String, conversationId: String, request: UpdateChatMetaRequest): ChatMetaDocument {
    val id = "${userId}_$conversationId"
    var doc = mongoTemplate.findById(id, ChatMetaDocument::class.java)
    if (doc == null) {
      doc = ChatMetaDocument(id = id, userId = userId, conversationId = conversationId)
      mongoTemplate.save(doc)
    }

    val update = Update()
    request.isArchived?.let { update.set("isArchived", it) }
    request.isMuted?.let { update.set("isMuted", it) }
    request.isPinned?.let {
      update.set("isPinned", it)
      if (it) update.set("pinnedAt", System.currentTimeMillis()) else update.unset("pinnedAt")
    }
    request.mutedUntil?.let { update.set("mutedUntil", it) }

    if (update.updateOperations.isNotEmpty()) {
      mongoTemplate.updateFirst(Query(Criteria.where("id").isEqualTo(id)), update, ChatMetaDocument::class.java)
    }
    return mongoTemplate.findById(id, ChatMetaDocument::class.java)!!
  }
}

@RestController
@RequestMapping("/api/chat-meta")
class ChatMetaController(
  private val chatMetaService: ChatMetaService
) {

  @GetMapping("/{conversationId}")
  fun getMeta(authentication: Authentication, @PathVariable conversationId: String): ChatMetaDto {
    val doc = chatMetaService.getMeta(authentication.name, conversationId)
      ?: return ChatMetaDto(conversationId, false, false, false, null)
    return ChatMetaDto.from(doc)
  }

  @PutMapping("/{conversationId}")
  fun updateMeta(
    authentication: Authentication,
    @PathVariable conversationId: String,
    @RequestBody req: UpdateChatMetaRequest
  ): ChatMetaDto {
    val doc = chatMetaService.updateMeta(authentication.name, conversationId, req)
    return ChatMetaDto.from(doc)
  }

  @GetMapping("/archived")
  fun listArchived(authentication: Authentication): List<ChatMetaDto> {
    return chatMetaService.listArchived(authentication.name).map { ChatMetaDto.from(it) }
  }

  @GetMapping("/muted")
  fun listMuted(authentication: Authentication): List<ChatMetaDto> {
    return chatMetaService.listMuted(authentication.name).map { ChatMetaDto.from(it) }
  }

  @GetMapping("/pinned")
  fun listPinned(authentication: Authentication): List<ChatMetaDto> {
    return chatMetaService.listPinned(authentication.name).map { ChatMetaDto.from(it) }
  }
}
