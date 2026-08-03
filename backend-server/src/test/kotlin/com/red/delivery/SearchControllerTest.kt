package com.red.delivery

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for SearchController DTOs.
 */
class SearchControllerTest {

  @Test
  fun `SearchResults has correct structure`() {
    val results = SearchController.SearchResults(
      messages = emptyList(),
      users = emptyList(),
      groups = emptyList(),
      total = 0
    )
    assertEquals(0, results.total)
    assertTrue(results.messages.isEmpty())
    assertTrue(results.users.isEmpty())
    assertTrue(results.groups.isEmpty())
  }

  @Test
  fun `UserSearchResult has required fields`() {
    val result = SearchController.UserSearchResult(
      id = "user1",
      fullName = "Test User",
      email = "test@example.com"
    )
    assertEquals("user1", result.id)
    assertEquals("Test User", result.fullName)
    assertEquals("test@example.com", result.email)
  }

  @Test
  fun `GroupSearchResult has required fields`() {
    val result = SearchController.GroupSearchResult(
      id = "grp_1",
      name = "Test Group",
      memberCount = 5
    )
    assertEquals("grp_1", result.id)
    assertEquals("Test Group", result.name)
    assertEquals(5, result.memberCount)
  }
}
