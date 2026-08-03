package com.red.feature.chat

import com.red.core.models.PublicUserDto
import com.red.core.models.UserView
import retrofit2.Response
import retrofit2.http.*

/**
 * Contact management API — browse, search, and manage contacts.
 */
interface ContactApi {
    @GET("api/contacts")
    suspend fun getContacts(): Response<List<PublicUserDto>>

    @POST("api/contacts/add")
    suspend fun addContact(@Body request: Map<String, String>): Response<Unit>

    @DELETE("api/contacts/{userId}")
    suspend fun removeContact(@Path("userId") userId: String): Response<Unit>

    @GET("api/contacts/sync")
    suspend fun syncPhoneContacts(@Query("phones") phones: String): Response<List<PublicUserDto>>
}

/**
 * Block / unblock API — mirrors the backend BlockController.
 */
interface BlockApi {
    @POST("api/block/{userId}")
    suspend fun blockUser(@Path("userId") userId: String): Response<Unit>

    @DELETE("api/block/{userId}")
    suspend fun unblockUser(@Path("userId") userId: String): Response<Unit>

    @GET("api/block/list")
    suspend fun getBlockedUsers(): Response<List<PublicUserDto>>

    @GET("api/block/check/{userId}")
    suspend fun isBlocked(@Path("userId") userId: String): Response<Map<String, Boolean>>
}

/**
 * Notification API — mirrors the backend NotificationController.
 */
interface NotificationApi {
    @GET("api/notifications/pending")
    suspend fun getPendingNotifications(): Response<List<NotificationDto>>

    @GET("api/notifications/count")
    suspend fun getNotificationCount(): Response<Map<String, Long>>

    @POST("api/notifications/mark-read")
    suspend fun markAsRead(@Body ids: List<String>): Response<Unit>

    @POST("api/notifications/mark-all-read")
    suspend fun markAllAsRead(): Response<Unit>
}

data class NotificationDto(
    val id: String,
    val type: String,       // MESSAGE, CALL, STORY, SYSTEM
    val title: String,
    val body: String,
    val fromUserId: String?,
    val timestamp: Long,
    val read: Boolean = false
)
