package com.red.core.models

import com.squareup.moshi.JsonClass

enum class UserStatus { PENDING, APPROVED, REJECTED, BANNED }

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val status: UserStatus,
    val role: String = "USER",
    val createdAt: Long = 0
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val user: User
)

@JsonClass(generateAdapter = true)
data class StatusResponse(
    val status: UserStatus
)

@JsonClass(generateAdapter = true)
data class PublicUserDto(
    val id: String,
    val fullName: String,
    val status: UserStatus
)
