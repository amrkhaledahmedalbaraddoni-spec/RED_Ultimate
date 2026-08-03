package com.red.delivery

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

// ── Document ────────────────────────────────────────────────────────────────

@Document(collection = "groups")
data class GroupDocument(
  @Id val id: String,
  val name: String,
  val description: String = "",
  val avatarUrl: String? = null,
  val ownerId: String,
  val memberIds: List<String>,
  val adminIds: List<String>,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

// ── DTOs ────────────────────────────────────────────────────────────────────

data class CreateGroupRequest(
  val name: String,
  val description: String = "",
  val memberIds: List<String>
)

data class GroupDto(
  val id: String,
  val name: String,
  val description: String,
  val avatarUrl: String?,
  val ownerId: String,
  val memberIds: List<String>,
  val adminIds: List<String>,
  val createdAt: Long,
  val memberCount: Int
) {
  companion object {
    fun from(doc: GroupDocument) = GroupDto(
      id = doc.id,
      name = doc.name,
      description = doc.description,
      avatarUrl = doc.avatarUrl,
      ownerId = doc.ownerId,
      memberIds = doc.memberIds,
      adminIds = doc.adminIds,
      createdAt = doc.createdAt,
      memberCount = doc.memberIds.size
    )
  }
}

data class UpdateGroupRequest(
  val name: String? = null,
  val description: String? = null,
  val avatarUrl: String? = null
)

data class AddMemberRequest(
  val userId: String
)

data class RemoveMemberRequest(
  val userId: String
)

data class PromoteAdminRequest(
  val userId: String
)

// ── Service ─────────────────────────────────────────────────────────────────

@Service
class GroupService(
  private val mongoTemplate: MongoTemplate
) {

  fun createGroup(ownerId: String, request: CreateGroupRequest): GroupDocument {
    val groupId = "grp_" + UuidV7.generate()
    val allMembers = (request.memberIds + ownerId).distinct()
    val doc = GroupDocument(
      id = groupId,
      name = request.name,
      description = request.description,
      ownerId = ownerId,
      memberIds = allMembers,
      adminIds = listOf(ownerId)
    )
    mongoTemplate.save(doc)
    return doc
  }

  fun getGroup(groupId: String): GroupDocument? {
    return mongoTemplate.findById(groupId, GroupDocument::class.java)
  }

  fun listUserGroups(userId: String): List<GroupDocument> {
    val query = Query(Criteria.where("memberIds").isEqualTo(userId))
    return mongoTemplate.find(query, GroupDocument::class.java)
  }

  fun updateGroup(groupId: String, request: UpdateGroupRequest, userId: String): GroupDocument {
    val doc = getGroup(groupId)
      ?: throw IllegalArgumentException("Group not found")
    if (doc.ownerId != userId && doc.adminIds.contains(userId).not()) {
      throw IllegalArgumentException("Only group admins can update the group")
    }
    val update = Update().set("updatedAt", System.currentTimeMillis())
    request.name?.let { update.set("name", it) }
    request.description?.let { update.set("description", it) }
    request.avatarUrl?.let { update.set("avatarUrl", it) }
    mongoTemplate.updateFirst(Query(Criteria.where("id").isEqualTo(groupId)), update, GroupDocument::class.java)
    return getGroup(groupId)!!
  }

  fun addMember(groupId: String, userId: String, addedBy: String): GroupDocument {
    val doc = getGroup(groupId)
      ?: throw IllegalArgumentException("Group not found")
    if (doc.ownerId != addedBy && doc.adminIds.contains(addedBy).not()) {
      throw IllegalArgumentException("Only group admins can add members")
    }
    if (doc.memberIds.contains(userId)) {
      throw IllegalArgumentException("User is already a member")
    }
    val update = Update()
      .addToSet("memberIds", userId)
      .set("updatedAt", System.currentTimeMillis())
    mongoTemplate.updateFirst(Query(Criteria.where("id").isEqualTo(groupId)), update, GroupDocument::class.java)
    return getGroup(groupId)!!
  }

  fun removeMember(groupId: String, userId: String, removedBy: String): GroupDocument {
    val doc = getGroup(groupId)
      ?: throw IllegalArgumentException("Group not found")
    if (doc.ownerId != removedBy && doc.adminIds.contains(removedBy).not() && userId != removedBy) {
      throw IllegalArgumentException("Only group admins can remove members, or you can leave yourself")
    }
    if (userId == doc.ownerId) {
      throw IllegalArgumentException("Owner cannot leave the group. Transfer ownership first.")
    }
    val update = Update()
      .pull("memberIds", userId)
      .pull("adminIds", userId)
      .set("updatedAt", System.currentTimeMillis())
    mongoTemplate.updateFirst(Query(Criteria.where("id").isEqualTo(groupId)), update, GroupDocument::class.java)
    return getGroup(groupId)!!
  }

  fun promoteAdmin(groupId: String, userId: String, promotedBy: String): GroupDocument {
    val doc = getGroup(groupId)
      ?: throw IllegalArgumentException("Group not found")
    if (doc.ownerId != promotedBy) {
      throw IllegalArgumentException("Only the owner can promote admins")
    }
    if (!doc.memberIds.contains(userId)) {
      throw IllegalArgumentException("User is not a member")
    }
    val update = Update()
      .addToSet("adminIds", userId)
      .set("updatedAt", System.currentTimeMillis())
    mongoTemplate.updateFirst(Query(Criteria.where("id").isEqualTo(groupId)), update, GroupDocument::class.java)
    return getGroup(groupId)!!
  }

  fun deleteGroup(groupId: String, userId: String) {
    val doc = getGroup(groupId)
      ?: throw IllegalArgumentException("Group not found")
    if (doc.ownerId != userId) {
      throw IllegalArgumentException("Only the owner can delete the group")
    }
    mongoTemplate.remove(doc)
  }
}

// ── Controller ──────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/groups")
class GroupController(
  private val groupService: GroupService
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createGroup(authentication: Authentication, @RequestBody req: CreateGroupRequest): GroupDto {
    val doc = groupService.createGroup(authentication.name, req)
    return GroupDto.from(doc)
  }

  @GetMapping
  fun listGroups(authentication: Authentication): List<GroupDto> {
    return groupService.listUserGroups(authentication.name).map { GroupDto.from(it) }
  }

  @GetMapping("/{groupId}")
  fun getGroup(authentication: Authentication, @PathVariable groupId: String): GroupDto {
    val doc = groupService.getGroup(groupId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found")
    if (!doc.memberIds.contains(authentication.name)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group")
    }
    return GroupDto.from(doc)
  }

  @PutMapping("/{groupId}")
  fun updateGroup(authentication: Authentication, @PathVariable groupId: String, @RequestBody req: UpdateGroupRequest): GroupDto {
    val doc = groupService.updateGroup(groupId, req, authentication.name)
    return GroupDto.from(doc)
  }

  @PostMapping("/{groupId}/members")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun addMember(authentication: Authentication, @PathVariable groupId: String, @RequestBody req: AddMemberRequest) {
    groupService.addMember(groupId, req.userId, authentication.name)
  }

  @DeleteMapping("/{groupId}/members/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeMember(authentication: Authentication, @PathVariable groupId: String, @PathVariable userId: String) {
    groupService.removeMember(groupId, userId, authentication.name)
  }

  @PostMapping("/{groupId}/admins")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun promoteAdmin(authentication: Authentication, @PathVariable groupId: String, @RequestBody req: PromoteAdminRequest) {
    groupService.promoteAdmin(groupId, req.userId, authentication.name)
  }

  @DeleteMapping("/{groupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteGroup(authentication: Authentication, @PathVariable groupId: String) {
    groupService.deleteGroup(groupId, authentication.name)
  }
}
