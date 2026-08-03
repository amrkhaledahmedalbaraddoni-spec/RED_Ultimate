package com.red.config

import com.red.auth.UserEntity
import com.red.auth.UserRepository
import com.red.auth.UserRole
import com.red.auth.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * First-run operator bootstrap. If no ADMIN user exists and credentials are supplied via env vars
 * (RED_BOOTSTRAP_ADMIN_EMAIL / RED_BOOTSTRAP_ADMIN_PASSWORD), an approved admin is created so the
 * approval workflow is reachable. No default credentials are ever created implicitly.
 */
@Configuration
class AdminBootstrap(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder
) {

  private val log = LoggerFactory.getLogger(javaClass)

  @Bean
  fun bootstrapAdmin(
    @Value("\${red.bootstrap.admin.email:}") email: String,
    @Value("\${red.bootstrap.admin.password:}") password: String
  ): ApplicationRunner = ApplicationRunner {
    if (email.isBlank() || password.isBlank()) {
      log.info("No bootstrap admin configured (set RED_BOOTSTRAP_ADMIN_EMAIL/PASSWORD to enable).")
      return@ApplicationRunner
    }
    val adminExists = userRepository.findAll().any { it.role == UserRole.ADMIN }
    if (adminExists) {
      return@ApplicationRunner
    }
    val existing = userRepository.findByEmail(email)
    val admin = (existing ?: UserEntity(
      email = email,
      fullName = "RED Operator",
      passwordHash = passwordEncoder.encode(password),
      status = UserStatus.APPROVED
    )).let { user ->
      user.role = UserRole.ADMIN
      user.status = UserStatus.APPROVED
      if (existing != null) user.passwordHash = passwordEncoder.encode(password)
      user
    }
    userRepository.save(admin)
    log.info("Bootstrap admin '{}' created/promoted.", email)
  }
}
