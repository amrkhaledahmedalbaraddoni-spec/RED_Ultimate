package com.red.auth

import com.red.security.AuditLogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Admin-only endpoints for the approval workflow and account lifecycle. Secured by
 * `hasRole('ADMIN')` in [com.red.config.SecurityConfig].
 */
@RestController
@RequestMapping("/api/admin/users")
class AdminApprovalController(
  private val approvalService: ApprovalService,
  private val userRepository: UserRepository,
  private val auditLogService: AuditLogService
) {

  @GetMapping("/pending")
  fun pending(): List<UserView> = approvalService.pending().map { UserView.from(it) }

  @GetMapping
  fun all(): List<UserView> = userRepository.findAll().map { UserView.from(it) }

  @PostMapping("/{userId}/approve")
  fun approve(@PathVariable userId: String): ResponseEntity<UserView> {
    val result = approvalService.approve(userId)
    auditLogService.log(userId, "USER_APPROVED", userId)
    return ResponseEntity.ok(UserView.from(result))
  }

  @PostMapping("/{userId}/reject")
  fun reject(@PathVariable userId: String): ResponseEntity<UserView> {
    val result = approvalService.reject(userId)
    auditLogService.log(userId, "USER_REJECTED", userId)
    return ResponseEntity.ok(UserView.from(result))
  }

  @PostMapping("/{userId}/ban")
  fun ban(@PathVariable userId: String): ResponseEntity<UserView> {
    val result = approvalService.ban(userId)
    auditLogService.log(userId, "USER_BANNED", userId)
    return ResponseEntity.ok(UserView.from(result))
  }

  /** Promote a user to admin (e.g. the first operator). */
  @PostMapping("/{userId}/promote")
  fun promote(@PathVariable userId: String): ResponseEntity<Any> {
    val user = userRepository.findById(userId).orElseThrow()
    user.role = UserRole.ADMIN
    auditLogService.log(userId, "USER_PROMOTED", userId)
    return ResponseEntity.ok(UserView.from(userRepository.save(user)))
  }

  /** Backwards-compatible single-param endpoint used by some dashboards. */
  @PostMapping("/approve")
  fun approveByEmail(
    @RequestParam email: String,
    @RequestParam(defaultValue = "APPROVED") status: String
  ): ResponseEntity<Any> {
    val user = userRepository.findByEmail(email)
      ?: return ResponseEntity.notFound().build()
    val updated = when (status.uppercase()) {
      "BANNED" -> approvalService.ban(user.id)
      "REJECTED" -> approvalService.reject(user.id)
      else -> approvalService.approve(user.id)
    }
    auditLogService.log(email, "USER_STATUS_CHANGED", user.id, "status=$status")
    return ResponseEntity.ok(UserView.from(updated))
  }
}
