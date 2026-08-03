package com.red.core.delivery

import com.red.core.database.RedDatabase
import com.red.feature.chat.ChatApi
import com.red.feature.chat.StoredMessageDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline sync service. When the app reconnects after being offline,
 * this service fetches all pending messages from the server and
 * stores them in the local Room database.
 */
@Singleton
class OfflineSyncService @Inject constructor(
    private val chatApi: ChatApi,
    private val database: RedDatabase,
    private val identity: ClientIdentity
) {
    private var lastKnownSequence: Long = 0L

    /**
     * Syncs pending messages from the server.
     * Called when the WebSocket reconnects or on app start.
     */
    suspend fun syncPendingMessages(): Int {
        val lastSeq = database.messageDao().lastSequence("") ?: 0L
        var syncedCount = 0

        try {
            val response = chatApi.getPendingMessages(since = lastSeq)
            if (response.isSuccessful) {
                val messages = response.body() ?: emptyList()
                for (msg in messages) {
                    val entity = MessageEntity(
                        id = msg.id,
                        conversationId = msg.conversationId,
                        senderId = msg.senderId,
                        receiverId = msg.receiverId,
                        payload = msg.payload,
                        timestamp = msg.timestamp,
                        sequenceNumber = msg.sequenceNumber,
                        status = MessageStatus.DELIVERED
                    )
                    database.messageDao().upsert(entity)
                    syncedCount++
                }
            }
        } catch (_: Exception) { }

        return syncedCount
    }

    /**
     * Syncs messages for a specific conversation.
     */
    suspend fun syncConversation(conversationId: String): Int {
        val lastSeq = database.messageDao().lastSequence(conversationId) ?: 0L
        var syncedCount = 0

        try {
            val response = chatApi.getMessages(conversationId, since = lastSeq)
            if (response.isSuccessful) {
                val messages = response.body() ?: emptyList()
                for (msg in messages) {
                    val entity = MessageEntity(
                        id = msg.id,
                        conversationId = msg.conversationId,
                        senderId = msg.senderId,
                        receiverId = msg.receiverId,
                        payload = msg.payload,
                        timestamp = msg.timestamp,
                        sequenceNumber = msg.sequenceNumber,
                        status = MessageStatus.DELIVERED
                    )
                    database.messageDao().upsert(entity)
                    syncedCount++
                }
            }
        } catch (_: Exception) { }

        return syncedCount
    }
}
