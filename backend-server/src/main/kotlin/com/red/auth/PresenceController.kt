package com.red.auth

import com.red.websocket.PresenceService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * Presence API — allows clients to check who is online and get user last-seen timestamps.
 */
@RestController
@RequestMapping("/api/presence")
class PresenceController(
  private val presenceService: PresenceService,
  private val userRepository: UserRepository
) {

  data class PresenceInfo(
    val userId: String,
    val online: Boolean,
    val lastSeenAt: Long
  )

  @GetMapping("/online")
  fun onlineUsers(authentication: Authentication): List<PresenceInfo> {
    val onlineIds = presenceService.onlineUserIds()
    return onlineIds.map { userId ->
      val user = userRepository.findById(userId).orElse(null)
      PresenceInfo(
        userId = userId,
        online = true,
        lastSeenAt = user?.lastSeenAt ?: 0L
      )
    }
  }

  @GetMapping("/check/{userId}")
  fun checkPresence(@PathVariable userId: String): PresenceInfo {
    val user = userRepository.findById(userId).orElse(null)
    return PresenceInfo(
      userId = userId,
      online = presenceService.isOnline(userId),
      lastSeenAt = user?.lastSeenAt ?: 0L
    )
  }
}
