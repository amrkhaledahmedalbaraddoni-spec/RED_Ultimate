package com.red.delivery

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * Search API — full-text search across messages, conversations, and groups.
 */
@RestController
@RequestMapping("/api/search")
class SearchController(
  private val mongoTemplate: MongoTemplate
) {

  data class SearchResults(
    val messages: List<StoredMessage>,
    val users: List<UserSearchResult>,
    val groups: List<GroupSearchResult>,
    val total: Int
  )

  data class UserSearchResult(
    val id: String,
    val fullName: String,
    val email: String
  )

  data class GroupSearchResult(
    val id: String,
    val name: String,
    val memberCount: Int
  )

  @GetMapping
  fun search(
    authentication: Authentication,
    @RequestParam q: String,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int
  ): SearchResults {
    val userId = authentication.name
    val messages = searchMessages(userId, q, page, size)
    val users = searchUsers(q, size)
    val groups = searchGroups(userId, q, size)
    return SearchResults(
      messages = messages,
      users = users,
      groups = groups,
      total = messages.size + users.size + groups.size
    )
  }

  @GetMapping("/messages")
  fun searchMessages(
    authentication: Authentication,
    @RequestParam q: String,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int
  ): List<StoredMessage> {
    return searchMessages(authentication.name, q, page, size)
  }

  private fun searchMessages(userId: String, query: String, page: Int, size: Int): List<StoredMessage> {
    val criteria = Criteria()
      .orOperator(
        Criteria.where("senderId").isEqualTo(userId),
        Criteria.where("receiverId").isEqualTo(userId)
      )
      .and("payload").regex(query, "i")

    val q = Query(criteria)
      .skip(page.toLong() * size)
      .limit(size)

    return mongoTemplate.find(q, MessageDocument::class.java)
      .map { StoredMessage.from(it) }
  }

  private fun searchUsers(query: String, limit: Int): List<UserSearchResult> {
    val criteria = Criteria().orOperator(
      Criteria.where("fullName").regex(query, "i"),
      Criteria.where("email").regex(query, "i")
    )
    val q = Query(criteria).limit(limit)
    return mongoTemplate.find(q, org.bson.Document::class.java, "users")
      .map { doc ->
        UserSearchResult(
          id = doc.getString("_id") ?: "",
          fullName = doc.getString("fullName") ?: "",
          email = doc.getString("email") ?: ""
        )
      }
  }

  private fun searchGroups(userId: String, query: String, limit: Int): List<GroupSearchResult> {
    val criteria = Criteria()
      .and("memberIds").isEqualTo(userId)
      .and("name").regex(query, "i")

    val q = Query(criteria).limit(limit)
    return mongoTemplate.find(q, GroupDocument::class.java)
      .map { doc ->
        GroupSearchResult(
          id = doc.id,
          name = doc.name,
          memberCount = doc.memberIds.size
        )
      }
  }
}
