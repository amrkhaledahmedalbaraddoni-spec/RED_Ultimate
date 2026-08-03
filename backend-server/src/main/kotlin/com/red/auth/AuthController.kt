package com.red.auth

import com.red.config.JwtService
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class RegisterRequest(
  @field:NotBlank val fullName: String,
  @field:Email @field:NotBlank val email: String,
  @field:Size(min = 8) val password: String
)

data class LoginRequest(
  @field:Email @field:NotBlank val email: String,
  @field:NotBlank val password: String
)

data class UserView(
  val id: String,
  val email: String,
  val fullName: String,
  val status: UserStatus,
  val role: UserRole,
  val createdAt: Long
) {
  companion object {
    fun from(u: UserEntity) = UserView(u.id, u.email, u.fullName, u.status, u.role, u.createdAt)
  }
}

data class AuthResponse(val token: String, val user: UserView)
data class StatusResponse(val status: UserStatus)

@RestController
@RequestMapping("/api/auth")
class AuthController(
  private val userRepository: UserRepository,
  private val approvalService: ApprovalService,
  private val passwordEncoder: PasswordEncoder,
  private val jwtService: JwtService
) {

  @PostMapping("/register")
  fun register(@RequestBody req: RegisterRequest): ResponseEntity<Any> {
    if (userRepository.existsByEmail(req.email)) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "EMAIL_TAKEN"))
    }
    val saved = userRepository.save(
      UserEntity(
        email = req.email,
        fullName = req.fullName,
        passwordHash = passwordEncoder.encode(req.password),
        status = UserStatus.PENDING
      )
    )
    return ResponseEntity.ok(
      mapOf("status" to UserStatus.PENDING.name, "user" to UserView.from(saved))
    )
  }

  @PostMapping("/login")
  fun login(@RequestBody req: LoginRequest): ResponseEntity<Any> {
    val user = userRepository.findByEmail(req.email)
      ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "INVALID_CREDENTIALS"))

    if (!passwordEncoder.matches(req.password, user.passwordHash)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "INVALID_CREDENTIALS"))
    }

    return when (user.status) {
      UserStatus.APPROVED -> ResponseEntity.ok(
        AuthResponse(jwtService.issue(user.id, user.email, user.role), UserView.from(user))
      )
      UserStatus.PENDING -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "PENDING_APPROVAL"))
      UserStatus.REJECTED -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "ACCOUNT_REJECTED"))
      UserStatus.BANNED -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "ACCOUNT_BANNED"))
    }
  }

  @GetMapping("/status")
  fun status(@RequestHeader("Authorization") auth: String): ResponseEntity<Any> {
    val token = auth.removePrefix("Bearer ").trim()
    val claims = jwtService.parse(token)
      ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "INVALID_TOKEN"))
    val user = userRepository.findById(claims.subject).orElse(null)
      ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "USER_NOT_FOUND"))
    return ResponseEntity.ok(StatusResponse(user.status))
  }
}
