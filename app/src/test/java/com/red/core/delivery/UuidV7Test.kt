package com.red.core.delivery

import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for UUID v7 generator.
 */
class UuidV7Test {

    @Test
    fun `generated UUID should be valid format`() {
        val uuid = UuidV7.now()
        // Should not throw
        UUID.fromString(uuid)
    }

    @Test
    fun `generated UUIDs should be unique`() {
        val uuids = (1..1000).map { UuidV7.now() }
        assertEquals(uuids.size, uuids.toSet().size)
    }

    @Test
    fun `UUIDs should be roughly time-ordered`() {
        val uuid1 = UuidV7.now()
        Thread.sleep(10)
        val uuid2 = UuidV7.now()
        // UUID v7 should be monotonically increasing
        assertTrue(uuid1 < uuid2)
    }

    @Test
    fun `UUID should have version 7`() {
        val uuid = UuidV7.now()
        val parsed = UUID.fromString(uuid)
        // Version is in the top 4 bits of the 7th octet
        val version = (parsed.mostSignificantBits shr 12) and 0xF
        assertEquals(7, version.toInt())
    }
}
