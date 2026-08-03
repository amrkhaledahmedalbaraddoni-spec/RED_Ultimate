package com.red.auth

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/users")
class UserController(
  private val userRepository: UserRepository
) {

  @GetMapping("/{id}")
  fun getPublicProfile(@PathVariable id: String): PublicUserView {
    val user = userRepository.findById(id).orElseThrow {
      ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }
    return PublicUserView(id = user.id, fullName = user.fullName, status = user.status)
  }
}

data class PublicUserView(
  val id: String,
  val fullName: String,
  val status: UserStatus
)
