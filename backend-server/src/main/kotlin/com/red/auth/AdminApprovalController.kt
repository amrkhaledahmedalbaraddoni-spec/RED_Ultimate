package com.red.auth

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Admin-only endpoints for the approval workflow and account lifecycle. Secured by
 * `hasRole('ADMIN')` in [com.red.config.SecurityConfig].
 */
@RestController
@RequestMapping("/api/admin/users")
class AdminApprovalController(
  private val approvalService: ApprovalService,
  private val userRepository: UserRepository
) {

  @GetMapping("/pending")
  fun pending(): List<UserView> = approvalService.pending().map { UserView.from(it) }

  @GetMapping
  fun all(): List<UserView> = userRepository.findAll().map { UserView.from(it) }

  @PostMapping("/{userId}/approve")
  fun approve(@PathVariable userId: String): ResponseEntity<UserView> =
    ResponseEntity.ok(UserView.from(approvalService.approve(userId)))

  @PostMapping("/{userId}/reject")
  fun reject(@PathVariable userId: String): ResponseEntity<UserView> =
    ResponseEntity.ok(UserView.from(approvalService.reject(userId)))

  @PostMapping("/{userId}/ban")
  fun ban(@PathVariable userId: String): ResponseEntity<UserView> =
    ResponseEntity.ok(UserView.from(approvalService.ban(userId)))

  /** Promote a user to admin (e.g. the first operator). */
  @PostMapping("/{userId}/promote")
  fun promote(@PathVariable userId: String): ResponseEntity<Any> {
    val user = userRepository.findById(userId).orElseThrow()
    user.role = UserRole.ADMIN
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
    return ResponseEntity.ok(UserView.from(updated))
  }
}
