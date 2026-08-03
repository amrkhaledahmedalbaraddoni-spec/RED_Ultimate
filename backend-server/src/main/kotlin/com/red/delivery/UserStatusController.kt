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
 * User status / emoji status — like WhatsApp/Telegram status updates.
 */

@Document(collection = "user_statuses")
data class UserStatusDocument(
  @Id val id: String,  // userId
  val emoji: String = "😊",
  val text: String = "",
  val updatedAt: Long = System.currentTimeMillis()
)

data class SetStatusRequest(
  val emoji: String = "😊",
  val text: String = ""
)

data class UserStatusDto(
  val userId: String,
  val emoji: String,
  val text: String,
  val updatedAt: Long
) {
  companion object {
    fun from(doc: UserStatusDocument) = UserStatusDto(
      userId = doc.id,
      emoji = doc.emoji,
      text = doc.text,
      updatedAt = doc.updatedAt
    )
  }
}

@Service
class UserStatusService(
  private val mongoTemplate: MongoTemplate
) {

  fun setStatus(userId: String, request: SetStatusRequest): UserStatusDocument {
    var doc = mongoTemplate.findById(userId, UserStatusDocument::class.java)
    if (doc == null) {
      doc = UserStatusDocument(id = userId, emoji = request.emoji, text = request.text)
      mongoTemplate.save(doc)
    } else {
      val update = Update()
        .set("emoji", request.emoji)
        .set("text", request.text)
        .set("updatedAt", System.currentTimeMillis())
      mongoTemplate.updateFirst(Query(Criteria.where("id").isEqualTo(userId)), update, UserStatusDocument::class.java)
      doc = mongoTemplate.findById(userId, UserStatusDocument::class.java)!!
    }
    return doc
  }

  fun getStatus(userId: String): UserStatusDocument? {
    return mongoTemplate.findById(userId, UserStatusDocument::class.java)
  }

  fun getMultipleStatuses(userIds: List<String>): List<UserStatusDocument> {
    val query = Query(Criteria.where("id").isEqualTo(userIds))
    return mongoTemplate.find(query, UserStatusDocument::class.java)
  }

  fun clearStatus(userId: String) {
    val update = Update()
      .set("emoji", "😊")
      .set("text", "")
      .set("updatedAt", System.currentTimeMillis())
    mongoTemplate.updateFirst(Query(Criteria.where("id").isEqualTo(userId)), update, UserStatusDocument::class.java)
  }
}

@RestController
@RequestMapping("/api/status")
class UserStatusController(
  private val userStatusService: UserStatusService
) {

  @PutMapping
  fun setStatus(authentication: Authentication, @RequestBody req: SetStatusRequest): UserStatusDto {
    val doc = userStatusService.setStatus(authentication.name, req)
    return UserStatusDto.from(doc)
  }

  @GetMapping("/{userId}")
  fun getStatus(@PathVariable userId: String): UserStatusDto {
    val doc = userStatusService.getStatus(userId)
      ?: return UserStatusDto(userId, "😊", "", System.currentTimeMillis())
    return UserStatusDto.from(doc)
  }

  @DeleteMapping
  fun clearStatus(authentication: Authentication): UserStatusDto {
    userStatusService.clearStatus(authentication.name)
    return UserStatusDto(authentication.name, "😊", "", System.currentTimeMillis())
  }
}
