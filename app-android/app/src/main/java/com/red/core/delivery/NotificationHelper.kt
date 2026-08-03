package com.red.core.delivery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.red.R

/**
 * Helper for creating local notifications for incoming messages and calls.
 */
object NotificationHelper {

    private const val CHANNEL_MESSAGES = "red_messages"
    private const val CHANNEL_CALLS = "red_calls"
    private const val CHANNEL_STORIES = "red_stories"

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
                setShowBadge(true)
            }

            val callChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming call notifications"
                enableVibration(true)
            }

            val storyChannel = NotificationChannel(
                CHANNEL_STORIES,
                "Stories",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "New story notifications"
            }

            manager.createNotificationChannels(listOf(messageChannel, callChannel, storyChannel))
        }
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        messagePreview: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(senderName)
            .setContentText(messagePreview)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        manager.notify(notificationId, notification)
    }

    fun showCallNotification(
        context: Context,
        callerName: String,
        notificationId: Int = 9999
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_CALLS)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Incoming Call")
            .setContentText(callerName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .build()

        manager.notify(notificationId, notification)
    }
}
