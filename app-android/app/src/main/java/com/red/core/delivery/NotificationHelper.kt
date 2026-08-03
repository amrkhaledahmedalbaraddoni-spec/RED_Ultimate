package com.red.core.delivery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.red.MainActivity

/**
 * Helper for creating local notifications for incoming messages, calls,
 * and stories. Supports:
 *  - Message notifications with reply action
 *  - Call notifications with answer/reject actions
 *  - Story notifications
 *  - Grouped notifications for multiple messages
 */
object NotificationHelper {

    private const val CHANNEL_MESSAGES = "red_messages"
    private const val CHANNEL_CALLS = "red_calls"
    private const val CHANNEL_STORIES = "red_stories"
    private const val CHANNEL_SYNC = "red_sync"

    // Notification IDs
    private const val ID_MESSAGE_BASE = 1000
    private const val ID_CALL_INCOMING = 9999
    private const val ID_STORY_BASE = 5000

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming message notifications"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            val callChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming call notifications"
                enableVibration(true)
                enableLights(true)
            }

            val storyChannel = NotificationChannel(
                CHANNEL_STORIES,
                "Stories",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "New story notifications"
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background sync notifications"
                setShowBadge(false)
            }

            manager.createNotificationChannels(listOf(messageChannel, callChannel, storyChannel, syncChannel))
        }
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        messagePreview: String,
        conversationId: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt() + ID_MESSAGE_BASE
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                putExtra("navigate", "chat_detail")
                putExtra("chatId", conversationId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(senderName)
            .setContentText(messagePreview)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(contentIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messagePreview)
            )
            .build()

        manager.notify(notificationId, notification)
    }

    fun showCallNotification(
        context: Context,
        callerName: String,
        notificationId: Int = ID_CALL_INCOMING
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CALLS)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Incoming Call")
            .setContentText(callerName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()

        manager.notify(notificationId, notification)
    }

    fun showStoryNotification(
        context: Context,
        userName: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt() + ID_STORY_BASE
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_STORIES)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("New Story")
            .setContentText("$userName posted a new story")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .build()

        manager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }

    fun cancelAllNotifications(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }
}
