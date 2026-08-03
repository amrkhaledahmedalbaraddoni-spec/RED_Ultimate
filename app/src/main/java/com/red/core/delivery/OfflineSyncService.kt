package com.red.core.delivery

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.red.MainActivity
import com.red.core.database.RedDatabase
import com.red.core.security.SessionManager
import com.red.feature.chat.ChatApi
import com.red.feature.chat.StoredMessageDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Offline sync service. When the app reconnects after being offline,
 * this service fetches all pending messages from the server and
 * stores them in the local Room database.
 *
 * Also provides a suspend function for use within coroutines.
 */
@AndroidEntryPoint
class OfflineSyncService : Service() {

    @Inject lateinit var chatApi: ChatApi
    @Inject lateinit var database: RedDatabase
    @Inject lateinit var identity: ClientIdentity
    @Inject lateinit var sessionManager: SessionManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createSyncNotification()
        startForeground(NOTIFICATION_ID, notification)

        scope.launch {
            val count = syncPendingMessages()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun createSyncNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, "red_sync")
            .setContentTitle("Syncing messages…")
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentIntent(pendingIntent)
            .setProgress(0, 0, true)
            .build()
    }

    private suspend fun syncPendingMessages(): Int {
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
                        payload = sessionManager.encryptMessage(msg.payload),
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

    companion object {
        private const val NOTIFICATION_ID = 1001

        /**
         * Start the sync service.
         */
        fun start(context: Context) {
            val intent = Intent(context, OfflineSyncService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        /**
         * Sync pending messages from the server (suspend function for use in coroutines).
         */
        suspend fun syncNow(
            chatApi: ChatApi,
            database: RedDatabase,
            identity: ClientIdentity,
            sessionManager: SessionManager
        ): Int {
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
                            payload = sessionManager.encryptMessage(msg.payload),
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
         * Sync messages for a specific conversation.
         */
        suspend fun syncConversation(
            chatApi: ChatApi,
            database: RedDatabase,
            conversationId: String,
            sessionManager: SessionManager
        ): Int {
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
                            payload = sessionManager.encryptMessage(msg.payload),
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
}
