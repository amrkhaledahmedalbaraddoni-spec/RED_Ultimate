package com.red.delivery

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

/**
 * Disappearing Messages — set a timer on conversations so messages auto-delete.
 */

data class SetDisappearingRequest(
  val conversationId: String,
  val durationSeconds: Long  // 0 = off, 86400 = 24h, 604800 = 7 days
)

data class DisappearingConfigDto(
  val conversationId: String,
  val durationSeconds: Long,
  val enabled: Boolean
)

@Service
class DisappearingMessageService(
  private val mongoTemplate: MongoTemplate
) {

  fun setDisappearing(userId: String, request: SetDisappearingRequest): DisappearingConfigDto {
    val query = Query(Criteria.where("conversationId").isEqualTo(request.conversationId))
    val update = Update()
      .set("durationSeconds", request.durationSeconds)
      .set("enabled", request.durationSeconds > 0)
      .set("updatedAt", System.currentTimeMillis())
    mongoTemplate.upsert(query, update, "disappearing_configs")
    return DisappearingConfigDto(
      conversationId = request.conversationId,
      durationSeconds = request.durationSeconds,
      enabled = request.durationSeconds > 0
    )
  }

  fun getDisappearingConfig(conversationId: String): DisappearingConfigDto {
    val query = Query(Criteria.where("conversationId").isEqualTo(conversationId))
    val doc = mongoTemplate.findOne(query, org.bson.Document::class.java, "disappearing_configs")
    return if (doc != null) {
      DisappearingConfigDto(
        conversationId = doc.getString("conversationId"),
        durationSeconds = doc.getLong("durationSeconds") ?: 0L,
        enabled = doc.getBoolean("enabled", false)
      )
    } else {
      DisappearingConfigDto(conversationId, 0L, false)
    }
  }

  /**
   * Scheduled cleanup — runs every 5 minutes.
   * Deletes messages older than their conversation's disappearing timer.
   */
  @Scheduled(fixedDelay = 300000)
  fun cleanupExpiredMessages() {
    val configs = mongoTemplate.find(
      Query(Criteria.where("enabled").isEqualTo(true)),
      org.bson.Document::class.java,
      "disappearing_configs"
    )
    for (config in configs) {
      val convId = config.getString("conversationId") ?: continue
      val duration = config.getLong("durationSeconds") ?: continue
      val cutoff = System.currentTimeMillis() - (duration * 1000)
      val query = Query(
        Criteria.where("conversationId").isEqualTo(convId)
          .and("timestamp").lt(cutoff)
      )
      mongoTemplate.remove(query, MessageDocument::class.java)
    }
  }
}

@RestController
@RequestMapping("/api/disappearing")
class DisappearingMessageController(
  private val disappearingService: DisappearingMessageService
) {

  @PutMapping
  fun setDisappearing(authentication: Authentication, @RequestBody req: SetDisappearingRequest): DisappearingConfigDto {
    return disappearingService.setDisappearing(authentication.name, req)
  }

  @GetMapping("/{conversationId}")
  fun getConfig(@PathVariable conversationId: String): DisappearingConfigDto {
    return disappearingService.getDisappearingConfig(conversationId)
  }
}
