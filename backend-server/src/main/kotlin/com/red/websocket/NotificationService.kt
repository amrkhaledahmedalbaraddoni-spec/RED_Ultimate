package com.red.websocket

import com.red.delivery.MessageDocument
import com.red.delivery.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

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

  data class PendingNotification(
    val userId: String,
    val type: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
  )

  /**
   * Enqueue a notification for an offline user.
   * The notification is stored in Redis with a 7-day TTL.
   */
  fun enqueueOfflineNotification(userId: String, type: String, content: String) {
    val key = "notify:$userId"
    val notification = "$type:$content:${System.currentTimeMillis()}"
    redis.opsForList().rightPush(key, notification)
    redis.expire(key, 7, java.util.concurrent.TimeUnit.DAYS)
    log.debug("Enqueued {} notification for offline user {}", type, userId)
  }

  /**
   * Retrieve pending notifications for a user (called when they reconnect).
   */
  fun getPendingNotifications(userId: String): List<String> {
    val key = "notify:$userId"
    val notifications = redis.opsForList().range(key, 0, -1) ?: emptyList()
    // Clear the queue after retrieval
    redis.delete(key)
    return notifications
  }

  /**
   * Count unread notifications for a user.
   */
  fun countPending(userId: String): Long {
    val key = "notify:$userId"
    return redis.opsForList().size(key) ?: 0L
  }
}
