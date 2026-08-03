package com.red.delivery

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Offline sync endpoint. A reconnecting client passes the highest sequence number it has seen and
 * receives everything newer — closing the "guaranteed delivery" loop for offline recipients.
 */
@RestController
@RequestMapping("/api/messages")
class SyncController(private val messageService: MessageService) {

  @GetMapping("/conversation")
  fun conversation(
    authentication: Authentication,
    @RequestParam conversationId: String,
    @RequestParam(defaultValue = "0") since: Long
  ): List<StoredMessage> =
    messageService.messagesForConversation(conversationId, since)

  @GetMapping("/pending")
  fun pending(
    authentication: Authentication,
    @RequestParam(defaultValue = "0") since: Long
  ): List<StoredMessage> =
    messageService.pendingForUser(authentication.name, since)
}
