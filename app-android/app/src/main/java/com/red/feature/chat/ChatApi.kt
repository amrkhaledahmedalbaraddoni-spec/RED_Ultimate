package com.red.feature.chat

import com.red.core.models.PublicUserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ChatApi {
    @GET("api/conversations")
    suspend fun conversations(): Response<List<ConversationDto>>

    @GET("api/users/{id}")
    suspend fun user(@Path("id") userId: String): Response<PublicUserDto>
}

data class ConversationDto(
    val conversationId: String,
    val peerId: String,
    val lastTimestamp: Long,
    val messageCount: Long
)
