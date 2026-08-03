package com.red.feature.chat

import com.red.core.models.PublicUserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
    @GET("api/conversations")
    suspend fun conversations(): Response<List<ConversationDto>>

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
