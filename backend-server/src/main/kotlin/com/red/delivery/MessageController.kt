package com.red.delivery

import com.red.websocket.ReadReceiptService
import com.red.websocket.TypingService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * Message operations: sync, read receipts, typing indicators, delete, and search.
 */
@RestController
@RequestMapping("/api/messages")
class MessageController(
  private val messageService: MessageService,
  private val readReceiptService: ReadReceiptService,
  private val typingService: TypingService
) {

  data class ReadReceiptRequest(
    val conversationId: String,
    val messageIds: List<String>
  )

  data class TypingRequest(
    val conversationId: String,
    val isTyping: Boolean
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
    readReceiptService.markAsRead(authentication.name, req.conversationId, req.messageIds)
  }

  // ── Typing indicators ────────────────────────────────────────────────────

  @PostMapping("/typing")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun sendTyping(authentication: Authentication, @RequestBody req: TypingRequest) {
    typingService.broadcastTyping(authentication.name, req.conversationId, req.isTyping)
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
