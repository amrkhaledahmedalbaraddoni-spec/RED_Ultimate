package com.red.delivery

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/conversations")
class ConversationController(
  private val conversationService: ConversationService
) {

  @GetMapping
  fun list(authentication: Authentication): List<ConversationSummary> {
    return conversationService.listForUser(authentication.name)
  }

  @GetMapping("/unread")
  fun unreadCount(authentication: Authentication): Map<String, Long> {
    val count = conversationService.unreadCountForUser(authentication.name)
    return mapOf("unread" to count)
  }
}
