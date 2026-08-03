package com.red.delivery

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/blocks")
class BlockController(
  private val blockService: BlockService
) {

  data class BlockRequest(val blockeeId: String)

  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun block(authentication: Authentication, @RequestBody req: BlockRequest) {
    blockService.blockUser(authentication.name, req.blockeeId)
  }

  @DeleteMapping("/{blockeeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun unblock(authentication: Authentication, @PathVariable blockeeId: String) {
    blockService.unblockUser(authentication.name, blockeeId)
  }

  @GetMapping
  fun listBlocked(authentication: Authentication): List<String> =
    blockService.getBlockedUsers(authentication.name)

  @GetMapping("/check/{userId}")
  fun checkBlock(authentication: Authentication, @PathVariable userId: String): Map<String, Boolean> =
    mapOf("blocked" to blockService.isBlocked(authentication.name, userId))
}
