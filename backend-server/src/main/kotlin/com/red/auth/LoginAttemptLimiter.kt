package com.red.auth

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class LoginAttemptLimiter(
  private val redis: StringRedisTemplate
) {
  companion object {
    private const val MAX_ATTEMPTS = 5L
    private const val WINDOW_MINUTES = 5L
  }

  fun isBlocked(key: String): Boolean {
    val attempts = redis.opsForValue().get("login:attempts:$key")?.toLongOrNull() ?: 0L
    return attempts >= MAX_ATTEMPTS
  }

  fun recordFailure(key: String) {
    val k = "login:attempts:$key"
    val n = redis.opsForValue().increment(k) ?: 1L
    if (n == 1L) redis.expire(k, WINDOW_MINUTES, TimeUnit.MINUTES)
  }

  fun clear(key: String) {
    redis.delete("login:attempts:$key")
  }
}
