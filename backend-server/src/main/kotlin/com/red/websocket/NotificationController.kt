package com.red.websocket

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * REST endpoint for retrieving and managing notifications.
 * Supports:
 *  - Get pending notifications
 *  - Get notification count
 *  - Mark individual notifications as read
 *  - Mark all notifications as read
 */
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
  private val notificationService: NotificationService
) {

  @GetMapping("/pending")
  fun pending(authentication: Authentication): List<NotificationService.NotificationEntry> {
    return notificationService.getPendingNotifications(authentication.name)
  }

  @GetMapping("/count")
  fun count(authentication: Authentication): Map<String, Long> =
    mapOf("count" to notificationService.countPending(authentication.name))

  @PostMapping("/mark-read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markRead(authentication: Authentication, @RequestBody ids: List<String>) {
    notificationService.markAsRead(authentication.name, ids)
  }

  @PostMapping("/mark-all-read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markAllRead(authentication: Authentication) {
    notificationService.markAllAsRead(authentication.name)
  }
}
