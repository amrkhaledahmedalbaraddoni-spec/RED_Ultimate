package com.red.auth


import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
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

  @GetMapping("/me")
  fun getMyProfile(authentication: Authentication): UserView {
    val user = userRepository.findById(authentication.name).orElseThrow {
      ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }
    return UserView.from(user)
  }

  data class UpdateProfileRequest(
    val fullName: String?,
    val phoneNumber: String?
  )

  @PutMapping("/me")
  fun updateMyProfile(authentication: Authentication, @RequestBody req: UpdateProfileRequest): UserView {
    val user = userRepository.findById(authentication.name).orElseThrow {
      ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }
    req.fullName?.let { if (it.isNotBlank()) user.fullName = it }
    req.phoneNumber?.let { user.phoneNumber = it }
    return UserView.from(userRepository.save(user))
  }

  @GetMapping("/search")
  fun searchUsers(@RequestParam q: String, authentication: Authentication): List<PublicUserView> {
    if (q.isBlank()) return emptyList()
    val results = userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(q, q)
    return results.filter { it.id != authentication.name && it.status == UserStatus.APPROVED }
      .map { PublicUserView(it.id, it.fullName, it.status) }
  }
}

data class PublicUserView(
  val id: String,
  val fullName: String,
  val status: UserStatus
)
