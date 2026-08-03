package com.red.security

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

/**
 * Rate limiting tracker — per-user, per-endpoint rate limiting.
 */

@Document(collection = "rate_limits")
data class RateLimitEntry(
  @Id val id: String,  // userId:endpoint
  val userId: String,
  val endpoint: String,
  val requestCount: Int,
  val windowStart: Long,
  val windowDurationMs: Long = 60000L  // 1 minute default
)

data class RateLimitStatus(
  val endpoint: String,
  val requestCount: Int,
  val limit: Int,
  val remaining: Int,
  val resetAt: Long
)

@Service
class RateLimitService(
  private val mongoTemplate: MongoTemplate
) {

  private val limits = mapOf(
    "/api/auth/login" to 5,
    "/api/auth/register" to 3,
    "/api/messages/typing" to 30,
    "/api/messages/read" to 30,
    "/api/stories" to 10,
    "/api/media/upload" to 20,
    "/api/search" to 10,
    "default" to 60
  )

  fun checkRateLimit(userId: String, endpoint: String): Boolean {
    val now = System.currentTimeMillis()
    val id = "${userId}:${endpoint}"
    val entry = mongoTemplate.findById(id, RateLimitEntry::class.java)

    if (entry == null) {
      mongoTemplate.save(RateLimitEntry(id, userId, endpoint, 1, now))
      return true
    }

    // Check if window has expired
    if (now - entry.windowStart > entry.windowDurationMs) {
      mongoTemplate.save(entry.copy(requestCount = 1, windowStart = now))
      return true
    }

    val limit = limits[endpoint] ?: limits["default"]!!
    return entry.requestCount < limit
  }

  fun getRateLimitStatus(userId: String, endpoint: String): RateLimitStatus {
    val now = System.currentTimeMillis()
    val id = "${userId}:${endpoint}"
    val entry = mongoTemplate.findById(id, RateLimitEntry::class.java)
    val limit = limits[endpoint] ?: limits["default"]!!

    return if (entry == null || now - entry.windowStart > entry.windowDurationMs) {
      RateLimitStatus(endpoint, 0, limit, limit, now + 60000L)
    } else {
      RateLimitStatus(endpoint, entry.requestCount, limit, (limit - entry.requestCount).coerceAtLeast(0), entry.windowStart + entry.windowDurationMs)
    }
  }

  fun getAllRateLimits(userId: String): List<RateLimitStatus> {
    val query = Query(Criteria.where("userId").isEqualTo(userId))
    val entries = mongoTemplate.find(query, RateLimitEntry::class.java)
    return entries.map { entry ->
      val limit = limits[entry.endpoint] ?: limits["default"]!!
      RateLimitStatus(entry.endpoint, entry.requestCount, limit, (limit - entry.requestCount).coerceAtLeast(0), entry.windowStart + entry.windowDurationMs)
    }
  }
}
