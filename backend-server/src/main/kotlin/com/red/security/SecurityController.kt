package com.red.security

import com.red.auth.ApprovalService
import com.red.websocket.PresenceService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Kill-switch: immediately bans a user and tears down any live WebSocket session so they can no
 * longer send or receive. Combined with remote-wipe on the device side, this is the emergency
 * revocation control.
 */
@RestController
@RequestMapping("/api/admin/security")
class SecurityController(
  private val approvalService: ApprovalService,
  private val presence: PresenceService,
  private val auditLogService: AuditLogService
) {

  @PostMapping("/kill-switch/{userId}")
  fun activate(@PathVariable userId: String): Map<String, Any> {
    approvalService.ban(userId)
    presence.sessionFor(userId)?.let { session ->
      runCatching { if (session.isOpen) session.close() }
    }
    auditLogService.log("SYSTEM", "KILL_SWITCH", userId, "User banned and session terminated")
    return mapOf("status" to "REVOKED", "target" to userId)
  }
}
