package com.red.feature.chat

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * Chat metadata API — archive, mute, pin conversations.
 * Mirrors the backend ChatMetaController at /api/chat-meta.
 */
interface ChatMetaApi {
    @GET("api/chat-meta/{conversationId}")
    suspend fun getMeta(@Path("conversationId") conversationId: String): Response<ChatMetaDto>

    @PUT("api/chat-meta/{conversationId}")
    suspend fun updateMeta(
        @Path("conversationId") conversationId: String,
        @Body request: UpdateChatMetaRequest
    ): Response<ChatMetaDto>

    @GET("api/chat-meta/archived")
    suspend fun listArchived(): Response<List<ChatMetaDto>>

    @GET("api/chat-meta/muted")
    suspend fun listMuted(): Response<List<ChatMetaDto>>

    @GET("api/chat-meta/pinned")
    suspend fun listPinned(): Response<List<ChatMetaDto>>
}

@JsonClass(generateAdapter = true)
data class ChatMetaDto(
    val conversationId: String,
    val isArchived: Boolean,
    val isMuted: Boolean,
    val isPinned: Boolean,
    val mutedUntil: Long? = null
)

@JsonClass(generateAdapter = true)
data class UpdateChatMetaRequest(
    val isArchived: Boolean? = null,
    val isMuted: Boolean? = null,
    val isPinned: Boolean? = null,
    val mutedUntil: Long? = null
)
