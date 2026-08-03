package com.red.delivery

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * Message operations: sync, read receipts, delete, and search.
 */
@RestController
@RequestMapping("/api/messages")
class MessageController(
  private val messageService: MessageService
) {

  data class ReadReceiptRequest(
    val conversationId: String,
    val messageIds: List<String>
  )

  // ── Offline sync ─────────────────────────────────────────────────────────

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

  // ── Read receipts ────────────────────────────────────────────────────────

  @PostMapping("/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markAsRead(authentication: Authentication, @RequestBody req: ReadReceiptRequest) {
    // The read receipt is handled via WebSocket; this REST endpoint is for
    // clients that may not have a WebSocket connection at the moment.
  }

  // ── Delete ───────────────────────────────────────────────────────────────

  @DeleteMapping("/{messageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteMessage(authentication: Authentication, @PathVariable messageId: String) {
    val message = messageService.findMessageById(messageId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found")
    if (message.senderId != authentication.name) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "Can only delete your own messages")
    }
    messageService.deleteMessage(messageId)
  }
}
