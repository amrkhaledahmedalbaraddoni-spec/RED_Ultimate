package com.red.core.delivery

/**
 * RED Master Delivery Engine — pure helpers for building outbound frames.
 */
object DeliveryEngine {

  fun prepareMessage(
    senderId: String,
    receiverId: String,
    conversationId: String,
    encryptedPayload: String,
    type: String = "TEXT"
  ): ChatFrame = ChatFrame(
    messageId = UuidV7.now(),
    senderId = senderId,
    receiverId = receiverId,
    conversationId = conversationId,
    payload = encryptedPayload,
    type = type
  )

  /**
   * System B (PSTN) isolation check. Returns true only when the requested system is "PSTN", i.e.
   * a hard boundary that prevents WebRTC/VoIP context from leaking into the GSM call path.
   */
  fun verifySystemIsolation(system: String): Boolean = system.equals("PSTN", ignoreCase = true)
}
