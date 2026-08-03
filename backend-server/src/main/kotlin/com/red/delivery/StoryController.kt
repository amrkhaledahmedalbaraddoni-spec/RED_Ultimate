package com.red.delivery

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/stories")
class StoryController(
  private val storyRepository: StoryRepository
) {

  data class StoryCreateRequest(
    @field:NotBlank val mediaUrl: String,
    @field:Positive val ttlMinutes: Long = 1440
  )

  data class StoryView(
    val id: String, val ownerId: String, val mediaUrl: String,
    val createdAt: Long, val expiresAt: Long
  )

  @GetMapping
  fun list(): List<StoryView> {
    val now = System.currentTimeMillis()
    return storyRepository.findByExpiresAtGreaterThan(now).map { StoryView(it.id, it.ownerId, it.mediaUrl, it.createdAt, it.expiresAt) }
  }

  @PostMapping
  fun create(authentication: Authentication, @Valid @RequestBody req: StoryCreateRequest): StoryView {
    val now = System.currentTimeMillis()
    val doc = StoryDocument(
      id = UuidV7.now(),
      ownerId = authentication.name,
      mediaUrl = req.mediaUrl,
      createdAt = now,
      expiresAt = now + req.ttlMinutes * 60_000
    )
    val saved = storyRepository.save(doc)
    return StoryView(saved.id, saved.ownerId, saved.mediaUrl, saved.createdAt, saved.expiresAt)
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun delete(authentication: Authentication, @PathVariable id: String) {
    val story = storyRepository.findById(id).orElseThrow {
      ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    if (story.ownerId != authentication.name) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }
    storyRepository.deleteById(id)
  }
}
