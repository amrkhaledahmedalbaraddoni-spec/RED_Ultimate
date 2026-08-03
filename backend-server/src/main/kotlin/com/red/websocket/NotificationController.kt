package com.red.websocket

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * REST endpoint for retrieving pending notifications when a client reconnects.
 */
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
  private val notificationService: NotificationService
) {

  @GetMapping("/pending")
  fun pending(authentication: Authentication): Map<String, Any> {
    val notifications = notificationService.getPendingNotifications(authentication.name)
    val count = notificationService.countPending(authentication.name)
    return mapOf(
      "notifications" to notifications,
      "count" to count
    )
  }

  @GetMapping("/count")
  fun count(authentication: Authentication): Map<String, Long> =
    mapOf("count" to notificationService.countPending(authentication.name))
}
