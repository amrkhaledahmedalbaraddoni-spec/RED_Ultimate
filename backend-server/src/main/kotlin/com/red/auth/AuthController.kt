package com.red.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val jwtService: com.red.config.JwtService,
  private val attemptLimiter: LoginAttemptLimiter
) {

  data class RegisterRequest(
    @field:NotBlank @field:Size(min = 2, max = 100) val fullName: String,
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String
  )

  data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
  )

  data class AuthResponse(val token: String, val user: UserView)
  data class StatusResponse(val status: UserStatus)

  @PostMapping("/register")
  fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<UserView> {
    if (userRepository.existsByEmail(req.email)) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build()
    }
    val user = UserEntity(
      id = com.red.delivery.UuidV7.now(),
      fullName = req.fullName,
      email = req.email,
      passwordHash = passwordEncoder.encode(req.password),
      status = UserStatus.PENDING,
      role = UserRole.USER
    )
    val saved = userRepository.save(user)
    return ResponseEntity.ok(UserView.from(saved))
  }

  @PostMapping("/login")
  fun login(@Valid @RequestBody req: LoginRequest, request: HttpServletRequest): ResponseEntity<Any> {
    val limiterKey = "${request.remoteAddr}:${req.email}"
    if (attemptLimiter.isBlocked(limiterKey)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .body(mapOf("error" to "Too many attempts. Try again later."))
    }
    val user = userRepository.findByEmail(req.email)
      ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid credentials"))
    if (!passwordEncoder.matches(req.password, user.passwordHash)) {
      attemptLimiter.recordFailure(limiterKey)
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid credentials"))
    }
    attemptLimiter.clear(limiterKey)
    val token = jwtService.generateToken(user.id, user.role.name)
    return ResponseEntity.ok(AuthResponse(token, UserView.from(user)))
  }

  @GetMapping("/status")
  fun status(authentication: Authentication): StatusResponse {
    val user = userRepository.findById(authentication.name).orElse(null)
          ?: return StatusResponse(UserStatus.PENDING)
    return StatusResponse(user.status)
  }

  data class ChangePasswordRequest(
    @field:NotBlank val currentPassword: String,
    @field:NotBlank @field:Size(min = 8) val newPassword: String
  )

  @PostMapping("/change-password")
  fun changePassword(authentication: Authentication, @Valid @RequestBody req: ChangePasswordRequest): ResponseEntity<Any> {
    val user = userRepository.findById(authentication.name).orElse(null)
      ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    if (!passwordEncoder.matches(req.currentPassword, user.passwordHash)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Current password is incorrect"))
    }
    user.passwordHash = passwordEncoder.encode(req.newPassword)
    userRepository.save(user)
    return ResponseEntity.ok(mapOf("message" to "Password changed successfully"))
  }
}
