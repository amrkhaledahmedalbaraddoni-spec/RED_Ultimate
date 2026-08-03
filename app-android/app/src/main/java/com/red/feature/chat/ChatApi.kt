package com.red.feature.chat

import com.red.core.models.PublicUserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
    @GET("api/conversations")
    suspend fun conversations(): Response<List<ConversationDto>>

    @GET("api/conversations/unread")
    suspend fun unreadCount(): Response<Map<String, Long>>

    @GET("api/users/{id}")
    suspend fun user(@Path("id") userId: String): Response<PublicUserDto>

    @GET("api/users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<PublicUserDto>>

    @GET("api/messages/conversation")
    suspend fun getMessages(
        @Query("conversationId") conversationId: String,
        @Query("since") since: Long = 0
    ): Response<List<StoredMessageDto>>

    @GET("api/messages/pending")
    suspend fun getPendingMessages(@Query("since") since: Long = 0): Response<List<StoredMessageDto>>

    @GET("api/notifications/pending")
    suspend fun getPendingNotifications(): Response<Map<String, Any>>

    @GET("api/notifications/count")
    suspend fun getNotificationCount(): Response<Map<String, Long>>

    @GET("api/presence/online")
    suspend fun getOnlineUsers(): Response<List<PresenceInfo>>

    @GET("api/presence/check/{userId}")
    suspend fun checkPresence(@Path("userId") userId: String): Response<PresenceInfo>
}

data class ConversationDto(
    val conversationId: String,
    val peerId: String,
    val lastTimestamp: Long,
    val messageCount: Long
)

data class StoredMessageDto(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val conversationId: String,
    val payload: String,
    val type: String,
    val timestamp: Long,
    val sequenceNumber: Long
)

data class PresenceInfo(
    val userId: String,
    val online: Boolean,
    val lastSeenAt: Long
)
