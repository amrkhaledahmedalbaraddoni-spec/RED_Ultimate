package com.red.admin

import com.red.delivery.StoryRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Auto-deletes expired stories (24h lifetime) every minute, mirroring the original Stories spec.
 */
@Component
class StoryCleanupScheduler(private val stories: StoryRepository) {

  private val log = LoggerFactory.getLogger(javaClass)

  @Scheduled(fixedDelay = 60_000L)
  fun cleanup() {
    val now = System.currentTimeMillis()
    val removed = stories.deleteByExpiresAtLessThan(now)
    if (removed > 0) {
      log.info("Deleted {} expired stories", removed)
    }
  }
}
