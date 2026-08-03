package com.red.websocket

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * Push notification service for offline users.
 * When a message arrives for a user who is not currently connected via WebSocket,
 * this service stores the message for later delivery and sends a push notification.
 *
 * For sovereign deployments, push notifications are handled via:
 * 1. A Redis-based queue that stores pending notifications
 * 2. A polling endpoint that the client checks when reconnecting
 * 3. (Optional) FCM integration for cloud-connected deployments
 */
@Service
class NotificationService(
  private val redis: StringRedisTemplate
) {

  private val log = LoggerFactory.getLogger(javaClass)

  data class NotificationEntry(
    val id: String,
    val type: String,       // MESSAGE, CALL, STORY, SYSTEM
    val title: String,
    val body: String,
    val fromUserId: String? = null,
    val timestamp: Long,
    val read: Boolean = false
  )

  /**
   * Enqueue a notification for an offline user.
   * The notification is stored in Redis with a 7-day TTL.
   */
  fun enqueueOfflineNotification(userId: String, type: String, content: String) {
    val key = "notify:$userId"
    val notification = "$type:$content:${System.currentTimeMillis()}"
    redis.opsForList().rightPush(key, notification)
    redis.expire(key, 7, TimeUnit.DAYS)
    log.debug("Enqueued {} notification for offline user {}", type, userId)
  }

  /**
   * Enqueue a structured notification for an offline user.
   */
  fun enqueueNotification(
    userId: String,
    type: String,
    title: String,
    body: String,
    fromUserId: String? = null
  ) {
    val key = "notify:$userId"
    val timestamp = System.currentTimeMillis()
    val id = "notif_${timestamp}_${(0..9999).random()}"
    val notification = "$id|$type|$title|$body|${fromUserId ?: ""}|$timestamp|false"
    redis.opsForList().rightPush(key, notification)
    redis.expire(key, 7, TimeUnit.DAYS)
    log.debug("Enqueued {} notification for user {}", type, userId)
  }

  /**
   * Retrieve pending notifications for a user (called when they reconnect).
   */
  fun getPendingNotifications(userId: String): List<NotificationEntry> {
    val key = "notify:$userId"
    val raw = redis.opsForList().range(key, 0, -1) ?: emptyList()
    return raw.mapNotNull { parseNotification(it) }
  }

  /**
   * Count unread notifications for a user.
   */
  fun countPending(userId: String): Long {
    val key = "notify:$userId"
    return redis.opsForList().size(key) ?: 0L
  }

  /**
   * Mark specific notifications as read.
   */
  fun markAsRead(userId: String, ids: List<String>) {
    val key = "notify:$userId"
    val raw = redis.opsForList().range(key, 0, -1) ?: return
    val updated = raw.map { entry ->
      val parsed = parseNotification(entry)
      if (parsed != null && parsed.id in ids) {
        "${parsed.id}|${parsed.type}|${parsed.title}|${parsed.body}|${parsed.fromUserId ?: ""}|${parsed.timestamp}|true"
      } else entry
    }
    redis.delete(key)
    updated.forEach { redis.opsForList().rightPush(key, it) }
  }

  /**
   * Mark all notifications as read for a user.
   */
  fun markAllAsRead(userId: String) {
    val key = "notify:$userId"
    val raw = redis.opsForList().range(key, 0, -1) ?: return
    val updated = raw.map { entry ->
      val parsed = parseNotification(entry)
      if (parsed != null) {
        "${parsed.id}|${parsed.type}|${parsed.title}|${parsed.body}|${parsed.fromUserId ?: ""}|${parsed.timestamp}|true"
      } else entry
    }
    redis.delete(key)
    updated.forEach { redis.opsForList().rightPush(key, it) }
  }

  private fun parseNotification(raw: String): NotificationEntry? {
    val parts = raw.split("|")
    return if (parts.size >= 7) {
      NotificationEntry(
        id = parts[0],
        type = parts[1],
        title = parts[2],
        body = parts[3],
        fromUserId = parts[4].ifBlank { null },
        timestamp = parts[5].toLongOrNull() ?: 0L,
        read = parts[6].toBooleanStrictOrNull() ?: false
      )
    } else {
      // Legacy format: "type:content:timestamp"
      val legacyParts = raw.split(":")
      if (legacyParts.size >= 2) {
        NotificationEntry(
          id = "legacy_${raw.hashCode()}",
          type = legacyParts[0],
          title = legacyParts[0],
          body = legacyParts[1],
          timestamp = legacyParts.getOrNull(2)?.toLongOrNull() ?: 0L,
          read = false
        )
      } else null
    }
  }
}
