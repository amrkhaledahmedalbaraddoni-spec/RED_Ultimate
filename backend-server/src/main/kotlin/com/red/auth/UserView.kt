package com.red.auth

/**
 * JSON-safe projection of [UserEntity] — never exposes the password hash.
 */
data class UserView(
  val id: String,
  val email: String,
  val fullName: String,
  val status: UserStatus,
  val role: UserRole,
  val phoneNumber: String?,
  val createdAt: Long
) {
  companion object {
    fun from(entity: UserEntity) = UserView(
      id = entity.id,
      email = entity.email,
      fullName = entity.fullName,
      status = entity.status,
      role = entity.role,
      phoneNumber = entity.phoneNumber,
      createdAt = entity.createdAt
    )
  }
}
