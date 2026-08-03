package com.red.security

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rate-limits")
class RateLimitController(
  private val rateLimitService: RateLimitService
) {

  @GetMapping
  fun getMyRateLimits(authentication: Authentication): List<RateLimitStatus> {
    return rateLimitService.getAllRateLimits(authentication.name)
  }

  @GetMapping("/{endpoint}")
  fun getEndpointRateLimit(authentication: Authentication, @PathVariable endpoint: String): RateLimitStatus {
    return rateLimitService.getRateLimitStatus(authentication.name, "/api/$endpoint")
  }
}
