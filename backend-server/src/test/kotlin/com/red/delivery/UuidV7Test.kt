package com.red.delivery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.Random

/**
 * Validates the RFC 9562 UUID v7 implementation:
 *  - version nibble is 7
 *  - variant bits are 10
 *  - the timestamp prefix dominates lexicographic ordering
 *  - uniqueness across many generations
 */
class UuidV7Test {

  @Test
  fun `version is 7`() {
    val id = UuidV7.now().replace("-", "")
    val versionNibble = id.substring(12, 13)
    assertEquals("7", versionNibble)
  }

  @Test
  fun `variant bits are 10`() {
    val id = UuidV7.now().replace("-", "")
    val variantHex = id.substring(16, 17)
    assertTrue(variantHex == "8" || variantHex == "9" || variantHex == "a" || variantHex == "b")
  }

  @Test
  fun `timestamp ordering is preserved`() {
    val rng = Random(42) // deterministic
    val earlier = UuidV7.generate(1_700_000_000_000L, rng)
    val later = UuidV7.generate(1_700_000_001_000L, rng)
    assertTrue(earlier < later)
  }

  @RepeatedTest(50)
  fun `ids are unique`() {
    val a = UuidV7.now()
    val b = UuidV7.now()
    assertNotEquals(a, b)
  }
}
