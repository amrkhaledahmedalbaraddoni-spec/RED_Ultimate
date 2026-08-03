package com.red.security

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Admin-only audit log viewer. Provides visibility into security-relevant events
 * such as user approvals, bans, kill-switch activations, and login attempts.
 */
@RestController
@RequestMapping("/api/admin/audit")
class AuditLogController(
  private val auditLogService: AuditLogService
) {

  data class AuditEventView(
    val id: String,
    val actorId: String,
    val action: String,
    val targetId: String?,
    val details: String?,
    val timestamp: Long
  )

  @GetMapping
  fun list(
    authentication: Authentication,
    @RequestParam(defaultValue = "0") since: Long,
    @RequestParam(defaultValue = "100") limit: Int
  ): List<AuditEventView> =
    auditLogService.recentEvents(since, limit).map {
      AuditEventView(it.id, it.actorId, it.action, it.targetId, it.details, it.timestamp)
    }

  @GetMapping("/count")
  fun count(@RequestParam(defaultValue = "0") since: Long): Map<String, Long> =
    mapOf("count" to auditLogService.countSince(since))
}
