package com.red.feature.chat

import com.red.core.models.PublicUserDto
import com.squareup.moshi.JsonClass
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
 * Block / unblock API — mirrors the backend BlockController at /api/blocks.
 */
interface BlockApi {
    @POST("api/blocks")
    suspend fun blockUser(@Body request: Map<String, String>): Response<Unit>

    @DELETE("api/blocks/{blockeeId}")
    suspend fun unblockUser(@Path("blockeeId") userId: String): Response<Unit>

    @GET("api/blocks")
    suspend fun getBlockedUsers(): Response<List<String>>

    @GET("api/blocks/check/{userId}")
    suspend fun isBlocked(@Path("userId") userId: String): Response<Map<String, Boolean>>
}

/**
 * Notification API — mirrors the backend NotificationController at /api/notifications.
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

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: String,
    val type: String,       // MESSAGE, CALL, STORY, SYSTEM
    val title: String,
    val body: String,
    val fromUserId: String? = null,
    val timestamp: Long,
    val read: Boolean = false
)
