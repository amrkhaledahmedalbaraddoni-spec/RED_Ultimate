package com.red.delivery

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * Message-level operations: read receipts, delete, and search.
 */
@RestController
@RequestMapping("/api/messages")
class MessageController(
  private val messageService: MessageService,
  private val messageRepository: MessageRepository
) {

  data class ReadReceiptRequest(
    val conversationId: String,
    val messageIds: List<String>
  )

  @PostMapping("/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markAsRead(authentication: Authentication, @RequestBody req: ReadReceiptRequest) {
    // The read receipt is handled via WebSocket; this REST endpoint is for
    // clients that may not have a WebSocket connection at the moment.
    // We simply acknowledge the receipt.
  }

  @DeleteMapping("/{messageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteMessage(authentication: Authentication, @PathVariable messageId: String) {
    val message = messageRepository.findById(messageId).orElseThrow {
      ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found")
    }
    // Only the sender can delete their own message
    if (message.senderId != authentication.name) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "Can only delete your own messages")
    }
    messageRepository.deleteById(messageId)
  }
}
