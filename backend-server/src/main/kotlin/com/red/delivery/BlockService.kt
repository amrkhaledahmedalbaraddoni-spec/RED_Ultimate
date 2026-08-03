package com.red.delivery

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * User block list. When a user blocks another, messages from the blocked user
 * are silently dropped and the blocked user cannot see the blocker's presence.
 */
@Document(collection = "blocks")
@CompoundIndex(name = "blocker_blockee", def = "{'blockerId': 1, 'blockeeId': 1}", unique = true)
class BlockDocument(
  @Id val id: String,
  @Indexed val blockerId: String,
  @Indexed val blockeeId: String,
  val createdAt: Long = System.currentTimeMillis()
)

interface BlockRepository : MongoRepository<BlockDocument, String> {
  fun findByBlockerId(blockerId: String): List<BlockDocument>
  fun existsByBlockerIdAndBlockeeId(blockerId: String, blockeeId: String): Boolean
  fun deleteByBlockerIdAndBlockeeId(blockerId: String, blockeeId: String): Long
}

/**
 * Block/mute service for user-level blocking.
 */
@org.springframework.stereotype.Service
class BlockService(
  private val blockRepository: BlockRepository
) {

  fun blockUser(blockerId: String, blockeeId: String) {
    if (!blockRepository.existsByBlockerIdAndBlockeeId(blockerId, blockeeId)) {
      blockRepository.save(BlockDocument(
        id = com.red.delivery.UuidV7.now(),
        blockerId = blockerId,
        blockeeId = blockeeId
      ))
    }
  }

  fun unblockUser(blockerId: String, blockeeId: String) {
    blockRepository.deleteByBlockerIdAndBlockeeId(blockerId, blockeeId)
  }

  fun isBlocked(blockerId: String, blockeeId: String): Boolean =
    blockRepository.existsByBlockerIdAndBlockeeId(blockerId, blockeeId)

  fun getBlockedUsers(blockerId: String): List<String> =
    blockRepository.findByBlockerId(blockerId).map { it.blockeeId }
}
