package com.red.admin

import com.red.delivery.MessageService
import com.red.delivery.StoryRepository
import com.red.websocket.PresenceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File

/**
 * Live system monitoring backed by real metrics (JVM, Mongo counts, presence, disk).
 */
@RestController
@RequestMapping("/api/admin/monitor")
class MonitorController(
  private val messageService: MessageService,
  private val storyRepository: StoryRepository,
  private val presence: PresenceService
) {

  @GetMapping("/health")
  fun health(): Map<String, Any> {
    val runtime = Runtime.getRuntime()
    val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
    val totalMb = runtime.totalMemory() / 1024 / 1024
    val maxMb = runtime.maxMemory() / 1024 / 1024
    val cpuCores = runtime.availableProcessors()
    return mapOf(
      "status" to "UP",
      "cpu_cores" to cpuCores,
      "ram_used_mb" to usedMb,
      "ram_total_mb" to totalMb,
      "ram_max_mb" to maxMb,
      "active_connections" to presence.onlineUserCount(),
      "disk_free_gb" to (File("/").usableSpace / 1024 / 1024 / 1024)
    )
  }

  @GetMapping("/stats")
  fun stats(): Map<String, Any> {
    val now = System.currentTimeMillis()
    val dayAgo = now - 24L * 60 * 60 * 1000
    return mapOf(
      "messages_24h" to messageService.countSince(dayAgo),
      "stories_active" to storyRepository.countByExpiresAtGreaterThan(now),
      "online_users" to presence.onlineUserCount(),
      "generated_at" to now
    )
  }
}
