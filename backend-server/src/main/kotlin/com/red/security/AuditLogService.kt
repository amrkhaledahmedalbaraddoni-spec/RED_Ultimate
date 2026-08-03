package com.red.security

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Service

/**
 * Immutable audit log for security-relevant events.
 * Events are append-only and never deleted.
 */
@Document(collection = "audit_log")
class AuditLogDocument(
  @Id val id: String,
  @Indexed val actorId: String,
  val action: String,
  val targetId: String?,
  val details: String?,
  @Indexed val timestamp: Long = System.currentTimeMillis()
)

interface AuditLogRepository : MongoRepository<AuditLogDocument, String> {
  fun findByActorIdOrderByTimestampDesc(actorId: String): List<AuditLogDocument>
  fun findByTimestampGreaterThanOrderByTimestampDesc(since: Long): List<AuditLogDocument>
  fun countByTimestampGreaterThan(since: Long): Long
}

@Service
class AuditLogService(
  private val auditLogRepository: AuditLogRepository
) {

  fun log(actorId: String, action: String, targetId: String? = null, details: String? = null) {
    auditLogRepository.save(AuditLogDocument(
      id = com.red.delivery.UuidV7.now(),
      actorId = actorId,
      action = action,
      targetId = targetId,
      details = details
    ))
  }

  fun recentEvents(since: Long, limit: Int = 100): List<AuditLogDocument> =
    auditLogRepository.findByTimestampGreaterThanOrderByTimestampDesc(since)
      .take(limit)

  fun countSince(since: Long): Long = auditLogRepository.countByTimestampGreaterThan(since)
}
