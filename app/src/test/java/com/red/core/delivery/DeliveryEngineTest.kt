package com.red.core.delivery

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Delivery Engine.
 */
class DeliveryEngineTest {

    @Test
    fun `prepareMessage should create valid ChatFrame`() {
        val frame = DeliveryEngine.prepareMessage(
            senderId = "user1",
            receiverId = "user2",
            conversationId = "conv1",
            encryptedPayload = "encrypted_data"
        )
        
        assertEquals("user1", frame.senderId)
        assertEquals("user2", frame.receiverId)
        assertEquals("conv1", frame.conversationId)
        assertEquals("encrypted_data", frame.payload)
        assertEquals("TEXT", frame.type)
        assertTrue(frame.messageId.isNotBlank())
        assertTrue(frame.timestamp > 0)
    }

    @Test
    fun `prepareMessage with custom type should work`() {
        val frame = DeliveryEngine.prepareMessage(
            senderId = "user1",
            receiverId = "user2",
            conversationId = "conv1",
            encryptedPayload = "image_data",
            type = "IMAGE"
        )
        
        assertEquals("IMAGE", frame.type)
    }

    @Test
    fun `verifySystemIsolation should return true for PSTN`() {
        assertTrue(DeliveryEngine.verifySystemIsolation("PSTN"))
        assertTrue(DeliveryEngine.verifySystemIsolation("pstn"))
        assertTrue(DeliveryEngine.verifySystemIsolation("Pstn"))
    }

    @Test
    fun `verifySystemIsolation should return false for non-PSTN`() {
        assertFalse(DeliveryEngine.verifySystemIsolation("VoIP"))
        assertFalse(DeliveryEngine.verifySystemIsolation("WEBRTC"))
        assertFalse(DeliveryEngine.verifySystemIsolation(""))
    }
}
