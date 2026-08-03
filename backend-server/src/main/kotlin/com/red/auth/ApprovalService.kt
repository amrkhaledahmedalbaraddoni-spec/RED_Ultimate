package com.red.auth

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Backs the "admin approval" gate. State lives in PostgreSQL so it survives restarts and is
 * visible across all backend instances.
 */
@Service
class ApprovalService(private val userRepository: UserRepository) {

  @Transactional
  fun approve(userId: String): UserEntity = mutate(userId) { it.status = UserStatus.APPROVED }

  @Transactional
  fun reject(userId: String): UserEntity = mutate(userId) { it.status = UserStatus.REJECTED }

  @Transactional
  fun ban(userId: String): UserEntity = mutate(userId) { it.status = UserStatus.BANNED }

  fun pending(): List<UserEntity> = userRepository.findByStatus(UserStatus.PENDING)

  fun isAllowed(user: UserEntity): Boolean = user.status == UserStatus.APPROVED

  private fun mutate(userId: String, block: (UserEntity) -> Unit): UserEntity {
    val user = userRepository.findById(userId).orElseThrow {
      IllegalArgumentException("Unknown user: $userId")
    }
    block(user)
    return userRepository.save(user)
  }
}
