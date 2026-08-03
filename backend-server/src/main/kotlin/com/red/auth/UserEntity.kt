package com.red.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

enum class UserStatus { PENDING, APPROVED, REJECTED, BANNED }
enum class UserRole { USER, ADMIN }

@Entity
@Table(name = "users")
class UserEntity(
  @Id
  val id: String = UUID.randomUUID().toString(),

  @Column(unique = true, nullable = false)
  val email: String,

  var passwordHash: String,

  var fullName: String,

  @Enumerated(EnumType.STRING)
  var status: UserStatus = UserStatus.PENDING,

  @Enumerated(EnumType.STRING)
  var role: UserRole = UserRole.USER,

  val createdAt: Long = System.currentTimeMillis(),

  /** Optional: a phone number bound to this account for PSTN calls. */
  var phoneNumber: String? = null
) {
  override fun equals(other: Any?): Boolean = other is UserEntity && other.id == id
  override fun hashCode(): Int = id.hashCode()
}

interface UserRepository : JpaRepository<UserEntity, String> {
  fun findByEmail(email: String): UserEntity?

  fun existsByEmail(email: String): Boolean

  @Query("SELECT u FROM UserEntity u WHERE u.status = :status ORDER BY u.createdAt ASC")
  fun findByStatus(@Param("status") status: UserStatus): List<UserEntity>
}
